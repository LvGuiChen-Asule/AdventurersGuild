package com.adventurersguild.quest;

import com.adventurersguild.data.DailyBoardManager;
import com.adventurersguild.data.QuestChainRegistry;
import com.adventurersguild.data.QuestRegistry;
import com.adventurersguild.data.ShopRegistry;
import com.adventurersguild.economy.Shop;
import com.adventurersguild.economy.ShopItem;
import com.adventurersguild.equipment.EquipmentEffects;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.chronicle.ChronicleManager;
import com.adventurersguild.chapter.MilestoneManager;
import com.adventurersguild.chapter.Chapter;
import com.adventurersguild.chapter.ChapterManager;
import com.adventurersguild.chapter.ChapterRegistry;
import com.adventurersguild.party.PartyManager;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.AdventurerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-authoritative quest logic for all quest types:
 * COLLECT / HUNT / EXPLORE / SURVIVE / TRANSPORT / ELITE,
 * plus quality multipliers, reputation, equipment bonuses, chains, daily board
 * refresh and shop purchases. Clients only send requests.
 */
public final class QuestManager {
    public static final String TAG_ELITE = "ag_elite";

    private static final int EXPIRE_CHECK_INTERVAL_TICKS = 20;
    private static final int MOVEMENT_CHECK_INTERVAL_TICKS = 40;
    private static final long TICKS_PER_SECOND = 20L;

    private QuestManager() {}

    public enum AcceptResult {
        OK,
        NOT_REGISTERED,
        UNKNOWN_QUEST,
        ALREADY_ACTIVE,
        ALREADY_COMPLETED,
        MAX_ACTIVE,
        LEVEL_TOO_LOW,
        REPUTATION_TOO_LOW,
        NOT_ON_BOARD
    }

    // ---------- registration ----------

    public static boolean register(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return false;
        }
        if (data.isRegistered()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.already_registered"));
            return false;
        }
        data.register();
        data.setPlayerName(player.getGameProfile().getName());
        data.setFirstRegisteredAt(player.serverLevel().getGameTime());
        ChronicleManager.recordEvent(player, ChronicleManager.EVENT_GUILD_REGISTER);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.registered"));
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.hall(player));
        return true;
    }

    // ---------- accept / abandon ----------

    public static AcceptResult acceptQuest(ServerPlayer player, String questId) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || !data.isRegistered()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.need_register"));
            return AcceptResult.NOT_REGISTERED;
        }
        Quest quest = QuestRegistry.get(questId);
        if (quest == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_not_found", questId));
            return AcceptResult.UNKNOWN_QUEST;
        }
        if (data.hasActiveQuest(questId)) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_already_active", questTitle(quest)));
            return AcceptResult.ALREADY_ACTIVE;
        }
        if (!data.hasFreeQuestSlot()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_slot_full", AdventurerData.MAX_ACTIVE_QUESTS));
            return AcceptResult.MAX_ACTIVE;
        }
        if (data.getLevel() < quest.getMinLevel()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_level_locked",
                    questTitle(quest), quest.getMinLevel()));
            return AcceptResult.LEVEL_TOO_LOW;
        }
        if (data.getReputation() < quest.getMinReputation()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_reputation_locked",
                    questTitle(quest), quest.getMinReputation()));
            return AcceptResult.REPUTATION_TOO_LOW;
        }
        if (!ChapterManager.isChapterQuestUnlocked(player, quest.getId())) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.chapter_locked"));
            return AcceptResult.NOT_ON_BOARD;
        }
        if (isChapterQuest(quest) && !chapterPrerequisiteMet(player, data, quest)) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_prerequisite"));
            return AcceptResult.NOT_ON_BOARD;
        }
        if (!isAcceptableOnBoard(player, data, quest)) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_not_on_board", questTitle(quest)));
            return AcceptResult.NOT_ON_BOARD;
        }

        long now = player.serverLevel().getGameTime();
        data.acceptQuest(quest, now);
        autoCompleteAcceptedQuest(player, data, quest);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_accepted", questTitle(quest)));
        sync(player);
        return AcceptResult.OK;
    }

    private static boolean isChapterQuest(Quest quest) {
        return ChapterRegistry.getChapterForQuest(quest.getId()) != null;
    }

    /**
     * Weak linearity: a main quest is acceptable when the previous chapter quest
     * is completed, or its milestone is already recorded (the player already
     * did the thing, so the mod just acknowledges it).
     */
    private static boolean chapterPrerequisiteMet(ServerPlayer player, AdventurerData data, Quest quest) {
        Chapter chapter = ChapterRegistry.getChapterForQuest(quest.getId());
        if (chapter == null) {
            return true;
        }
        int index = chapter.getQuestIds().indexOf(quest.getId());
        if (index <= 0) {
            return true;
        }
        String previous = chapter.getQuestIds().get(index - 1);
        if (data.isQuestCompleted(previous)) {
            return true;
        }
        Quest previousQuest = QuestRegistry.get(previous);
        return previousQuest != null && previousQuest.getType() == QuestType.MILESTONE
                && ChronicleManager.hasEvent(player, previousQuest.getTarget());
    }

    /** Immediately completes accepted MILESTONE / ACHIEVEMENT quests already satisfied. */
    private static void autoCompleteAcceptedQuest(ServerPlayer player, AdventurerData data, Quest quest) {
        QuestProgress progress = data.getActiveQuest(quest.getId());
        if (progress == null) {
            return;
        }
        boolean done = false;
        if (quest.getType() == QuestType.MILESTONE
                && ChronicleManager.hasEvent(player, quest.getTarget())) {
            progress.setProgress(1);
            done = true;
        } else if (quest.getType() == QuestType.ACHIEVEMENT
                && statValue(player, data, quest.getTarget()) >= quest.getAmount()) {
            progress.setProgress(quest.getAmount());
            done = true;
        }
        if (done) {
            completeQuest(player, data, progress, quest);
        }
    }

    /** Tutorial and chain quests are one-time; daily board quests are repeatable. */
    private static boolean isAcceptableOnBoard(ServerPlayer player, AdventurerData data, Quest quest) {
        if (quest.isTutorial()) {
            return !data.isQuestCompleted(quest.getId());
        }
        if (isChapterQuest(quest)) {
            return true;
        }
        if (isChainQuest(player, data, quest)) {
            return true;
        }
        return DailyBoardManager.isOnBoard(player.serverLevel(), quest.getId());
    }

    private static boolean isChainQuest(ServerPlayer player, AdventurerData data, Quest quest) {
        QuestChain chain = QuestChainRegistry.getByQuest(quest.getId());
        if (chain == null) {
            return false;
        }
        int stepIndex = chainStepIndex(chain, quest.getId());
        return stepIndex >= 0
                && data.getChainProgress(chain.getId()) >= stepIndex
                && chainFirstStepUnlocked(player, data, chain);
    }

    private static int chainStepIndex(QuestChain chain, String questId) {
        for (int i = 0; i < chain.getSteps().size(); i++) {
            if (chain.getSteps().get(i).getQuestId().equals(questId)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean chainFirstStepUnlocked(ServerPlayer player, AdventurerData data, QuestChain chain) {
        QuestChain.Step first = chain.getStep(0);
        if (first == null) {
            return true;
        }
        return switch (first.getUnlockType()) {
            case "level" -> data.getLevel() >= first.getUnlockValue();
            case "reputation" -> data.getReputation() >= first.getUnlockValue();
            default -> true;
        };
    }

    public static boolean abandonQuest(ServerPlayer player, String questId) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || !data.abandonQuest(questId)) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_not_active", questId));
            return false;
        }
        data.incrementAbandonedQuestCount();
        Quest quest = QuestRegistry.get(questId);
        player.sendSystemMessage(Component.translatable(
                "msg.adventurersguild.quest_abandoned", quest != null ? questTitle(quest) : questId));
        sync(player);
        return true;
    }

    /** Dev/test command: instantly completes an active quest (with rewards). */
    public static boolean forceComplete(ServerPlayer player, String questId) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return false;
        }
        QuestProgress progress = data.getActiveQuest(questId);
        if (progress == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_not_active", questId));
            return false;
        }
        Quest quest = QuestRegistry.get(questId);
        if (quest == null) {
            return false;
        }
        progress.setStatus(QuestStatus.IN_PROGRESS);
        progress.setProgress(quest.getAmount());
        completeQuest(player, data, progress, quest);
        return true;
    }

    // ---------- completion & rewards ----------

    private static void completeQuest(ServerPlayer player, AdventurerData data, QuestProgress progress, Quest quest) {
        if (progress.getStatus() == QuestStatus.COMPLETED) {
            return;
        }
        progress.setStatus(QuestStatus.COMPLETED);
        data.removeQuest(quest.getId());
        data.incrementCompletedQuestCount();
        data.recordQuestCompleted(quest.getId());
        if (data.getCompletedQuestCount() >= 100) {
            ChronicleManager.recordEvent(player, ChronicleManager.EVENT_100_QUESTS);
        }

        float multiplier = quest.getQuality().getRewardMultiplier();
        double expBonus = EquipmentEffects.getEffectSum(player, EquipmentEffects.EFFECT_QUEST_EXP);
        double goldBonus = EquipmentEffects.getEffectSum(player, EquipmentEffects.EFFECT_GOLD_REWARD);
        int exp = (int) Math.floor(quest.getExpReward() * multiplier * (1.0 + expBonus));
        int gold = (int) Math.floor(quest.getGoldReward() * multiplier * (1.0 + goldBonus));
        int reputation = quest.getReputationReward();

        int levelsGained = data.addExperience(exp);
        data.addGold(gold);
        data.addReputation(reputation);

        player.sendSystemMessage(Component.translatable("msg.adventurersguild.quest_completed", questTitle(quest))
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.reward_gold", gold)
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.reward_exp", exp)
                .withStyle(ChatFormatting.AQUA));
        if (reputation > 0) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.reward_reputation", reputation)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (levelsGained > 0) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.level_up", data.getLevel())
                    .withStyle(ChatFormatting.YELLOW));
        }
        advanceChain(player, data, quest);
        MilestoneManager.onQuestCompleted(player);
        PartyManager.onQuestCompleted(player);
        sync(player);
    }

    private static void advanceChain(ServerPlayer player, AdventurerData data, Quest quest) {
        for (QuestChain chain : QuestChainRegistry.list()) {
            int stepIndex = chainStepIndex(chain, quest.getId());
            if (stepIndex >= 0 && data.getChainProgress(chain.getId()) == stepIndex) {
                int next = stepIndex + 1;
                data.setChainProgress(chain.getId(), next);
                player.sendSystemMessage(Component.translatable("msg.adventurersguild.chain_advanced",
                                Component.translatable(chain.getTitleKey()), next)
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    // ---------- progress events (server side) ----------

    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) {
            return;
        }
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || !data.isRegistered()) {
            return;
        }

        boolean changed = false;
        boolean completed = false;
        for (QuestProgress progress : List.copyOf(data.getActiveQuests())) {
            if (!isProgressing(progress)) {
                continue;
            }
            Quest quest = QuestRegistry.get(progress.getQuestId());
            if (quest == null || quest.getType() != QuestType.COLLECT || !quest.matchesItem(stack)) {
                continue;
            }
            int remaining = quest.getAmount() - progress.getProgress();
            if (remaining <= 0) {
                continue;
            }
            progress.addProgress(Math.min(stack.getCount(), remaining));
            progress.markInProgress();
            changed = true;
            if (progress.getProgress() >= quest.getAmount()) {
                completeQuest(player, data, progress, quest);
                completed = true;
            }
        }
        if (changed && !completed) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer dying) {
            resetSurviveProgress(dying);
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        EntityType<?> killedType = event.getEntity().getType();
        boolean elite = event.getEntity().getPersistentData().getBoolean(TAG_ELITE);
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || !data.isRegistered()) {
            return;
        }

        boolean changed = false;
        boolean completed = false;
        for (QuestProgress progress : List.copyOf(data.getActiveQuests())) {
            if (!isProgressing(progress)) {
                continue;
            }
            Quest quest = QuestRegistry.get(progress.getQuestId());
            if (quest == null) {
                continue;
            }
            if (quest.getType() == QuestType.HUNT && quest.matchesEntity(killedType)) {
                progress.addProgress(1);
                changed = true;
            } else if (quest.getType() == QuestType.ELITE
                    && elite
                    && quest.matchesEntity(killedType)) {
                progress.addProgress(1);
                com.adventurersguild.player.ChronicleState eliteState =
                        AdventurerCapability.getChronicleState(player);
                if (eliteState != null) {
                    eliteState.incrementCounter("eliteKills", 1);
                    com.adventurersguild.chronicle.ChronicleManager.syncChronicle(player);
                }
                changed = true;
            } else {
                continue;
            }
            progress.markInProgress();
            if (progress.getProgress() >= quest.getAmount()) {
                completeQuest(player, data, progress, quest);
                completed = true;
            }
        }
        if (changed && !completed) {
            sync(player);
        }
    }

    /** Buffs natural spawns into elite variants while any player runs an ELITE quest for that type. */
    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (mob.getPersistentData().getBoolean(TAG_ELITE)) {
            return;
        }
        EntityType<?> type = mob.getType();
        boolean eliteQuestActive = false;
        for (ServerPlayer player : serverLevel.players()) {
            AdventurerData data = AdventurerCapability.get(player);
            if (data == null) {
                continue;
            }
            for (QuestProgress progress : data.getActiveQuests()) {
                Quest quest = QuestRegistry.get(progress.getQuestId());
                if (quest != null && quest.getType() == QuestType.ELITE && quest.matchesEntity(type)) {
                    eliteQuestActive = true;
                    break;
                }
            }
            if (eliteQuestActive) {
                break;
            }
        }
        if (!eliteQuestActive) {
            return;
        }

        mob.getPersistentData().putBoolean(TAG_ELITE, true);
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                    "ag_elite_health", maxHealth.getBaseValue() * 1.5, AttributeModifier.Operation.ADDITION));
        }
        mob.setHealth(mob.getMaxHealth());
        mob.setCustomName(Component.translatable("entity.adventurersguild.elite", mob.getType().getDescription()));
        mob.setCustomNameVisible(true);
    }

    // ---------- per-second objectives + expiry ----------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % EXPIRE_CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || data.getActiveQuestCount() == 0) {
            return;
        }

        long now = player.serverLevel().getGameTime();
        List<String> expired = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        boolean changed = false;
        boolean completed = false;
        for (QuestProgress progress : List.copyOf(data.getActiveQuests())) {
            Quest quest = QuestRegistry.get(progress.getQuestId());
            if (quest == null) {
                data.abandonQuest(progress.getQuestId());
                removed.add(progress.getQuestId());
                continue;
            }
            if (quest.getTimeLimitSeconds() > 0
                    && now - progress.getAcceptedAtTick() >= quest.getTimeLimitSeconds() * TICKS_PER_SECOND) {
                data.abandonQuest(progress.getQuestId());
                data.incrementAbandonedQuestCount();
                expired.add(progress.getQuestId());
                changed = true;
                continue;
            }
            if (!isProgressing(progress)) {
                continue;
            }
            switch (quest.getType()) {
                case EXPLORE -> {
                    if (playerInBiome(player, quest.getTarget())) {
                        progress.setProgress(1);
                        progress.markInProgress();
                        changed = true;
                        completeQuest(player, data, progress, quest);
                        completed = true;
                    }
                }
                case SURVIVE -> {
                    if (surviveConditionMet(player, quest)) {
                        progress.addProgress(1);
                        progress.markInProgress();
                        changed = true;
                        if (progress.getProgress() >= quest.getAmount()) {
                            completeQuest(player, data, progress, quest);
                            completed = true;
                        }
                    }
                }
                case TRANSPORT -> {
                    if (playerInDeliveryArea(player, quest)) {
                        int delivered = deliverItems(player, quest,
                                quest.getAmount() - progress.getProgress());
                        if (delivered > 0) {
                            progress.addProgress(delivered);
                            progress.markInProgress();
                            changed = true;
                            if (progress.getProgress() >= quest.getAmount()) {
                                completeQuest(player, data, progress, quest);
                                completed = true;
                            }
                        }
                    }
                }
                case MILESTONE -> {
                    if (ChronicleManager.hasEvent(player, quest.getTarget())) {
                        progress.setProgress(1);
                        progress.markInProgress();
                        changed = true;
                        completeQuest(player, data, progress, quest);
                        completed = true;
                    }
                }
                case ACHIEVEMENT -> {
                    if (statValue(player, data, quest.getTarget()) >= quest.getAmount()) {
                        progress.setProgress(quest.getAmount());
                        progress.markInProgress();
                        changed = true;
                        completeQuest(player, data, progress, quest);
                        completed = true;
                    }
                }
                default -> { }
            }
        }

        for (String questId : expired) {
            Quest quest = QuestRegistry.get(questId);
            player.sendSystemMessage(Component.translatable(
                            "msg.adventurersguild.quest_expired", quest != null ? questTitle(quest) : questId)
                    .withStyle(ChatFormatting.RED));
        }
        for (String questId : removed) {
            player.sendSystemMessage(Component.translatable(
                    "msg.adventurersguild.quest_removed", questId).withStyle(ChatFormatting.RED));
        }
        if (changed && !completed) {
            sync(player);
        }
    }

    /** Movement speed accessory effect (re-applied periodically, cheap). */
    @SubscribeEvent
    public static void onMovementTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % MOVEMENT_CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        EquipmentEffects.applyMoveSpeedModifier(player);
    }

    // ---------- equipment effects ----------

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            double bonus = EquipmentEffects.getEffectSum(player, EquipmentEffects.EFFECT_MINING_SPEED);
            if (bonus > 0) {
                event.setNewSpeed(event.getOriginalSpeed() * (1f + (float) bonus));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        double bonus = EquipmentEffects.getEffectSum(player, EquipmentEffects.EFFECT_HOSTILE_DAMAGE);
        if (bonus > 0) {
            event.setAmount(event.getAmount() * (1f + (float) bonus));
        }
    }

    // ---------- shop (V0.5) ----------

    public static boolean buyItem(ServerPlayer player, String shopId, int itemIndex) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return false;
        }
        Shop shop = ShopRegistry.get(shopId);
        if (shop == null || itemIndex < 0 || itemIndex >= shop.getItems().size()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.shop_invalid"));
            return false;
        }
        ShopItem shopItem = shop.getItems().get(itemIndex);
        if (data.getLevel() < shopItem.getMinLevel()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.shop_level_locked", shopItem.getMinLevel()));
            return false;
        }
        if (!data.spendGold(shopItem.getPrice())) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.shop_no_gold", shopItem.getPrice()));
            return false;
        }

        ResourceLocation itemId = ResourceLocation.tryParse(shopItem.getItemId());
        ItemStack stack = itemId != null
                ? new ItemStack(BuiltInRegistries.ITEM.get(itemId), shopItem.getCount())
                : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            data.addGold(shopItem.getPrice()); // refund
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.shop_invalid"));
            return false;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.shop_bought",
                stack.getHoverName(), shopItem.getCount(), shopItem.getPrice()));
        sync(player);
        return true;
    }

    // ---------- daily board refresh (V0.4) ----------

    public static int refreshBoard(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || !data.isRegistered()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.need_register"));
            return -1;
        }
        long day = DailyBoardManager.currentDay(player.serverLevel());
        int cost = data.getRefreshCost(day);
        if (cost > 0 && !data.spendGold(cost)) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.shop_no_gold", cost));
            return -1;
        }
        data.recordDailyRefresh(day);
        DailyBoardManager.reroll(player.serverLevel());
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.board_refreshed", cost));
        sync(player);
        return cost;
    }

    // ---------- objective helpers ----------

    /** INTERACT quest progress: called when the player interacts with a guild NPC. */
    public static void onNpcInteract(ServerPlayer player, String npcRole) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || !data.isRegistered()) {
            return;
        }
        boolean changed = false;
        boolean completed = false;
        for (QuestProgress progress : List.copyOf(data.getActiveQuests())) {
            if (!isProgressing(progress)) {
                continue;
            }
            Quest quest = QuestRegistry.get(progress.getQuestId());
            if (quest == null || quest.getType() != QuestType.INTERACT
                    || !quest.getTarget().equals(npcRole)) {
                continue;
            }
            progress.addProgress(1);
            progress.markInProgress();
            changed = true;
            if (progress.getProgress() >= quest.getAmount()) {
                completeQuest(player, data, progress, quest);
                completed = true;
            }
        }
        if (changed && !completed) {
            sync(player);
        }
    }

    private static int statValue(ServerPlayer player, AdventurerData data, String key) {
        if (key == null) {
            return 0;
        }
        return switch (key) {
            case "completed" -> data.getCompletedQuestCount();
            case "gold" -> (int) Math.min(Integer.MAX_VALUE, data.getGold());
            case "reputation" -> data.getReputation();
            case "level" -> data.getLevel();
            case "witherKills" -> data.getCounters().getOrDefault("witherKills", 0);
            case "dragonKills" -> data.getCounters().getOrDefault("dragonKills", 0);
            case "endCrystals" -> data.getCounters().getOrDefault("endCrystals", 0);
            case "dragonDamage" -> data.getCounters().getOrDefault("dragonDamage", 0);
            case "eliteKills" -> data.getCounters().getOrDefault("eliteKills", 0);
            case "loreCount" -> data.getLoreDiscovered().size();
            default -> 0;
        };
    }

    private static boolean isProgressing(QuestProgress progress) {
        return progress.getStatus() == QuestStatus.ACCEPTED || progress.getStatus() == QuestStatus.IN_PROGRESS;
    }

    private static boolean playerInBiome(ServerPlayer player, String target) {
        ResourceLocation biomeId = ResourceLocation.tryParse(target);
        if (biomeId == null) {
            return false;
        }
        Registry<Biome> biomes = player.level().registryAccess().registryOrThrow(Registries.BIOME);
        return biomes.getKey(player.level().getBiome(player.blockPosition()).value()).equals(biomeId);
    }

    private static boolean surviveConditionMet(ServerPlayer player, Quest quest) {
        String condition = quest.getExtra() == null || quest.getExtra().isBlank() ? "any" : quest.getExtra();
        return switch (condition) {
            case "night" -> player.level().isNight();
            case "biome" -> playerInBiome(player, quest.getTarget());
            case "dimension" -> {
                ResourceLocation dimension = ResourceLocation.tryParse(quest.getTarget());
                yield dimension != null && player.level().dimension().location().equals(dimension);
            }
            default -> true;
        };
    }

    private static void resetSurviveProgress(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return;
        }
        boolean changed = false;
        for (QuestProgress progress : data.getActiveQuests()) {
            Quest quest = QuestRegistry.get(progress.getQuestId());
            if (quest != null && quest.getType() == QuestType.SURVIVE && progress.getProgress() > 0) {
                progress.setProgress(0);
                changed = true;
            }
        }
        if (changed) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.survive_reset")
                    .withStyle(ChatFormatting.RED));
            sync(player);
        }
    }

    private static boolean playerInDeliveryArea(ServerPlayer player, Quest quest) {
        if (player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return false;
        }
        BlockPos target = deliveryTarget(player.serverLevel(), quest);
        if (target == null) {
            return false;
        }
        double radius = Math.max(8, quest.getRadius());
        return player.blockPosition().distSqr(target) <= radius * radius;
    }

    private static BlockPos deliveryTarget(ServerLevel level, Quest quest) {
        String extra = quest.getExtra() == null ? "" : quest.getExtra();
        if (extra.equals("spawn")) {
            return level.getSharedSpawnPos();
        }
        String[] parts = extra.split(",");
        if (parts.length < 2) {
            return null;
        }
        try {
            return new BlockPos(Integer.parseInt(parts[0].trim()), 0, Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int deliverItems(ServerPlayer player, Quest quest, int remaining) {
        if (remaining <= 0) {
            return 0;
        }
        int delivered = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty() || delivered >= remaining) {
                continue;
            }
            if (quest.matchesItem(stack)) {
                int take = Math.min(stack.getCount(), remaining - delivered);
                stack.shrink(take);
                delivered += take;
            }
        }
        return delivered;
    }

    private static Component questTitle(Quest quest) {
        return Component.translatable(quest.getTitleKey());
    }

    private static void sync(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.update(player));
    }
}
