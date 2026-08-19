package com.adventurersguild.network;

import com.adventurersguild.data.QuestRegistry;
import com.adventurersguild.data.DailyBoardManager;
import com.adventurersguild.chapter.Chapter;
import com.adventurersguild.chapter.ChapterManager;
import com.adventurersguild.chapter.ChapterRegistry;
import com.adventurersguild.data.QuestChainRegistry;
import com.adventurersguild.data.ShopRegistry;
import com.adventurersguild.economy.Shop;
import com.adventurersguild.guild.GuildWorldData;
import com.adventurersguild.party.AdventurerParty;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.AdventurerData;
import com.adventurersguild.player.ReputationData;
import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestChain;
import com.adventurersguild.quest.QuestProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server -> client data snapshot: quest definitions plus the player's
 * adventurer state. The client only renders this data; all decisions stay
 * server-side.
 */
public class GuildDataSyncPacket {
    public enum ScreenType {
        HALL,
        MY_QUESTS,
        ADVENTURER,
        SHOP,
        CHAINS,
        MAIN,
        UPDATE
    }

    private final ScreenType screen;
    private final boolean registered;
    private final int level;
    private final int experience;
    private final long gold;
    private final int completedQuestCount;
    private final int abandonedQuestCount;
    private final int reputation;
    private final int reputationTier;
    private final int freeRefreshesLeft;
    private final int nextRefreshCost;
    private final List<Quest> quests;
    private final List<String> boardQuestIds;
    private final List<String> completedQuestIds;
    private final List<ActiveQuestView> activeQuests;
    private final List<Shop> shops;
    private final List<QuestChain> chains;
    private final List<Integer> chainProgress;
    private final List<String> chapterIds;
    private final List<Boolean> chapterUnlocked;
    private final String partyName;
    private final int partyMemberCount;
    private final int partyLevel;

    public GuildDataSyncPacket(ScreenType screen, boolean registered, int level, int experience,
                           long gold, int completedQuestCount, int abandonedQuestCount,
                           int reputation, int reputationTier, int freeRefreshesLeft, int nextRefreshCost,
                           List<Quest> quests, List<String> boardQuestIds, List<String> completedQuestIds,
                           List<ActiveQuestView> activeQuests, List<Shop> shops,
                           List<QuestChain> chains, List<Integer> chainProgress,
                           List<String> chapterIds, List<Boolean> chapterUnlocked,
                           String partyName, int partyMemberCount, int partyLevel) {
        this.screen = screen;
        this.registered = registered;
        this.level = level;
        this.experience = experience;
        this.gold = gold;
        this.completedQuestCount = completedQuestCount;
        this.abandonedQuestCount = abandonedQuestCount;
        this.reputation = reputation;
        this.reputationTier = reputationTier;
        this.freeRefreshesLeft = freeRefreshesLeft;
        this.nextRefreshCost = nextRefreshCost;
        this.quests = quests;
        this.boardQuestIds = boardQuestIds;
        this.completedQuestIds = completedQuestIds;
        this.activeQuests = activeQuests;
        this.shops = shops;
        this.chains = chains;
        this.chainProgress = chainProgress;
        this.chapterIds = chapterIds;
        this.chapterUnlocked = chapterUnlocked;
        this.partyName = partyName;
        this.partyMemberCount = partyMemberCount;
        this.partyLevel = partyLevel;
    }

    public static GuildDataSyncPacket hall(ServerPlayer player) {
        return build(ScreenType.HALL, player);
    }

    public static GuildDataSyncPacket myQuests(ServerPlayer player) {
        return build(ScreenType.MY_QUESTS, player);
    }

    public static GuildDataSyncPacket adventurer(ServerPlayer player) {
        return build(ScreenType.ADVENTURER, player);
    }

    public static GuildDataSyncPacket shop(ServerPlayer player) {
        return build(ScreenType.SHOP, player);
    }

    public static GuildDataSyncPacket chains(ServerPlayer player) {
        return build(ScreenType.CHAINS, player);
    }

    public static GuildDataSyncPacket main(ServerPlayer player) {
        return build(ScreenType.MAIN, player);
    }

    public static GuildDataSyncPacket update(ServerPlayer player) {
        return build(ScreenType.UPDATE, player);
    }

    private static GuildDataSyncPacket build(ScreenType screen, ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        boolean registered = data != null && data.isRegistered();
        int level = data != null ? data.getLevel() : 1;
        int experience = data != null ? data.getExperience() : 0;
        long gold = data != null ? data.getGold() : 0;
        int completed = data != null ? data.getCompletedQuestCount() : 0;
        int abandoned = data != null ? data.getAbandonedQuestCount() : 0;
        int reputation = data != null ? data.getReputation() : 0;
        int reputationTier = ReputationData.getTier(reputation);
        long day = DailyBoardManager.currentDay(player.serverLevel());
        int freeRefreshes = data != null ? data.getFreeRefreshesLeft(day) : 3;
        int refreshCost = data != null ? data.getRefreshCost(day) : 0;

        List<Quest> quests = QuestRegistry.list();
        List<String> boardQuestIds = DailyBoardManager.getDailyQuestIds(player.serverLevel());
        List<String> completedQuestIds = data != null ? data.getCompletedQuestIds() : List.of();
        List<ActiveQuestView> active = new ArrayList<>();
        if (data != null) {
            long now = player.serverLevel().getGameTime();
            for (QuestProgress progress : data.getActiveQuests()) {
                Quest quest = QuestRegistry.get(progress.getQuestId());
                long remainingSeconds = -1;
                if (quest != null && quest.getTimeLimitSeconds() > 0) {
                    long deadlineTick = progress.getAcceptedAtTick() + quest.getTimeLimitSeconds() * 20L;
                    remainingSeconds = Math.max(0, deadlineTick - now) / 20L;
                }
                active.add(new ActiveQuestView(
                        progress.getQuestId(),
                        progress.getStatus().name(),
                        progress.getProgress(),
                        quest != null ? quest.getAmount() : 0,
                        quest != null ? quest.getTitleKey() : "quest.adventurersguild.unknown.title",
                        quest != null ? quest.getTimeLimitSeconds() : 0,
                        remainingSeconds
                ));
            }
        }
        List<QuestChain> chains = QuestChainRegistry.list();
        List<Integer> chainProgress = new ArrayList<>();
        for (QuestChain chain : chains) {
            chainProgress.add(data != null ? data.getChainProgress(chain.getId()) : 0);
        }
        List<String> chapterIds = new ArrayList<>();
        List<Boolean> chapterUnlocked = new ArrayList<>();
        for (Chapter chapter : ChapterRegistry.list()) {
            chapterIds.add(chapter.getId());
            chapterUnlocked.add(ChapterManager.isUnlocked(player, chapter));
        }
        String partyName = "";
        int partyMemberCount = 0;
        int partyLevel = 1;
        if (data != null && data.getPartyId() != null) {
            AdventurerParty party = GuildWorldData.get(player.serverLevel()).getParty(data.getPartyId());
            if (party != null) {
                partyName = party.getPartyName();
                partyMemberCount = party.getMembers().size();
                partyLevel = party.getLevel();
            }
        }
        return new GuildDataSyncPacket(screen, registered, level, experience, gold, completed, abandoned,
                reputation, reputationTier, freeRefreshes, refreshCost,
                quests, boardQuestIds, completedQuestIds, active, ShopRegistry.list(), chains, chainProgress,
                chapterIds, chapterUnlocked, partyName, partyMemberCount, partyLevel);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(screen.name());
        buf.writeBoolean(registered);
        buf.writeVarInt(level);
        buf.writeVarInt(experience);
        buf.writeLong(gold);
        buf.writeVarInt(completedQuestCount);
        buf.writeVarInt(abandonedQuestCount);
        buf.writeVarInt(reputation);
        buf.writeVarInt(reputationTier);
        buf.writeVarInt(freeRefreshesLeft);
        buf.writeVarInt(nextRefreshCost);
        buf.writeCollection(quests, (b, quest) -> quest.encode(b));
        buf.writeCollection(boardQuestIds, (b, id) -> b.writeUtf(id));
        buf.writeCollection(completedQuestIds, (b, id) -> b.writeUtf(id));
        buf.writeCollection(activeQuests, (b, view) -> ActiveQuestView.encode(view, b));
        buf.writeCollection(shops, (b, shop) -> shop.encode(b));
        buf.writeCollection(chains, (b, chain) -> chain.encode(b));
        buf.writeCollection(chainProgress, FriendlyByteBuf::writeVarInt);
        buf.writeCollection(chapterIds, FriendlyByteBuf::writeUtf);
        buf.writeCollection(chapterUnlocked, FriendlyByteBuf::writeBoolean);
        buf.writeUtf(partyName);
        buf.writeVarInt(partyMemberCount);
        buf.writeVarInt(partyLevel);
    }

    public static GuildDataSyncPacket decode(FriendlyByteBuf buf) {
        ScreenType screen = ScreenType.valueOf(buf.readUtf(32));
        boolean registered = buf.readBoolean();
        int level = buf.readVarInt();
        int experience = buf.readVarInt();
        long gold = buf.readLong();
        int completed = buf.readVarInt();
        int abandoned = buf.readVarInt();
        int reputation = buf.readVarInt();
        int reputationTier = buf.readVarInt();
        int freeRefreshes = buf.readVarInt();
        int refreshCost = buf.readVarInt();
        List<Quest> quests = buf.readList(Quest::decode);
        List<String> boardQuestIds = buf.readList(b -> b.readUtf(128));
        List<String> completedQuestIds = buf.readList(b -> b.readUtf(128));
        List<ActiveQuestView> active = buf.readList(ActiveQuestView::decode);
        List<Shop> shops = buf.readList(Shop::decode);
        List<QuestChain> chains = buf.readList(QuestChain::decode);
        List<Integer> chainProgress = buf.readList(FriendlyByteBuf::readVarInt);
        List<String> chapterIds = buf.readList(b -> b.readUtf(64));
        List<Boolean> chapterUnlocked = buf.readList(FriendlyByteBuf::readBoolean);
        String partyName = buf.readUtf(128);
        int partyMemberCount = buf.readVarInt();
        int partyLevel = buf.readVarInt();
        return new GuildDataSyncPacket(screen, registered, level, experience, gold, completed, abandoned,
                reputation, reputationTier, freeRefreshes, refreshCost,
                quests, boardQuestIds, completedQuestIds, active, shops, chains, chainProgress,
                chapterIds, chapterUnlocked, partyName, partyMemberCount, partyLevel);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> com.adventurersguild.client.ClientPacketHandlers.onGuildData(this)));
        context.setPacketHandled(true);
    }

    public ScreenType getScreen() { return screen; }
    public boolean isRegistered() { return registered; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public long getGold() { return gold; }
    public int getCompletedQuestCount() { return completedQuestCount; }
    public int getAbandonedQuestCount() { return abandonedQuestCount; }
    public int getReputation() { return reputation; }
    public int getReputationTier() { return reputationTier; }
    public int getFreeRefreshesLeft() { return freeRefreshesLeft; }
    public int getNextRefreshCost() { return nextRefreshCost; }
    public List<Quest> getQuests() { return quests; }
    public List<String> getBoardQuestIds() { return boardQuestIds; }
    public List<String> getCompletedQuestIds() { return completedQuestIds; }
    public boolean isQuestCompleted(String questId) { return completedQuestIds.contains(questId); }
    public List<ActiveQuestView> getActiveQuests() { return activeQuests; }
    public List<Shop> getShops() { return shops; }
    public List<QuestChain> getChains() { return chains; }
    public List<Integer> getChainProgress() { return chainProgress; }
    public List<String> getChapterIds() { return chapterIds; }
    public List<Boolean> getChapterUnlocked() { return chapterUnlocked; }
    public String getPartyName() { return partyName; }
    public int getPartyMemberCount() { return partyMemberCount; }
    public int getPartyLevel() { return partyLevel; }

    /** Client-side view of one active quest (server-computed snapshot). */
    public static class ActiveQuestView {
        private final String questId;
        private final String status;
        private final int progress;
        private final int target;
        private final String titleKey;
        private final int timeLimitSeconds;
        private long remainingSeconds;

        public ActiveQuestView(String questId, String status, int progress, int target,
                               String titleKey, int timeLimitSeconds, long remainingSeconds) {
            this.questId = questId;
            this.status = status;
            this.progress = progress;
            this.target = target;
            this.titleKey = titleKey;
            this.timeLimitSeconds = timeLimitSeconds;
            this.remainingSeconds = remainingSeconds;
        }

        public static void encode(ActiveQuestView view, FriendlyByteBuf buf) {
            buf.writeUtf(view.questId);
            buf.writeUtf(view.status);
            buf.writeVarInt(view.progress);
            buf.writeVarInt(view.target);
            buf.writeUtf(view.titleKey);
            buf.writeVarInt(view.timeLimitSeconds);
            buf.writeLong(view.remainingSeconds);
        }

        public static ActiveQuestView decode(FriendlyByteBuf buf) {
            return new ActiveQuestView(
                    buf.readUtf(128),
                    buf.readUtf(32),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf(256),
                    buf.readVarInt(),
                    buf.readLong()
            );
        }

        public String getQuestId() { return questId; }
        public String getStatus() { return status; }
        public int getProgress() { return progress; }
        public int getTarget() { return target; }
        public String getTitleKey() { return titleKey; }
        public int getTimeLimitSeconds() { return timeLimitSeconds; }
        public long getRemainingSeconds() { return remainingSeconds; }

        public void tickDownRemaining() {
            if (remainingSeconds > 0) {
                remainingSeconds--;
            }
        }
    }
}
