package com.adventurersguild.command;

import com.adventurersguild.data.QuestRegistry;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.guild.GuildWorldData;
import com.adventurersguild.npc.GuildNpcHandler;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.AdventurerData;
import com.adventurersguild.player.LevelData;
import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestManager;
import com.adventurersguild.quest.QuestProgress;
import com.adventurersguild.party.PartyManager;
import com.adventurersguild.chapter.Chapter;
import com.adventurersguild.chapter.ChapterRegistry;
import com.adventurersguild.chronicle.ChronicleManager;
import com.adventurersguild.npc.GuildNpcManager;
import com.adventurersguild.player.UnlockState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * /ag commands. All server-side.
 * Dev commands (givegold / addexp / complete / reset) require permission level 2.
 */
public final class AGCommands {
    private static final String PREFIX = "ag";

    private AGCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(PREFIX)
                .then(Commands.literal("register").executes(ctx -> register(player(ctx.getSource()))))
                .then(Commands.literal("info").executes(ctx -> info(player(ctx.getSource()))))
                .then(Commands.literal("quests").executes(ctx -> openHall(player(ctx.getSource()))))
                .then(Commands.literal("myquests").executes(ctx -> openMyQuests(player(ctx.getSource()))))
                .then(Commands.literal("adventurer").executes(ctx -> openAdventurer(player(ctx.getSource()))))
                .then(Commands.literal("shop").executes(ctx -> openShop(player(ctx.getSource()))))
                .then(Commands.literal("chains").executes(ctx -> openChains(player(ctx.getSource()))))
                .then(Commands.literal("refresh").executes(ctx -> refresh(player(ctx.getSource()))))
                .then(Commands.literal("guild")
                        .executes(ctx -> openMain(player(ctx.getSource())))
                        .then(Commands.literal("locate").executes(ctx -> guildLocate(player(ctx.getSource())))))
                .then(Commands.literal("chronicle").executes(ctx -> openChronicle(player(ctx.getSource()))))
                .then(Commands.literal("party")
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> partyCreate(player(ctx.getSource()),
                                                StringArgumentType.getString(ctx, "name")))))
                        .then(Commands.literal("invite")
                                .then(Commands.argument("target", StringArgumentType.word())
                                        .executes(ctx -> partyInvite(player(ctx.getSource()),
                                                ctx.getSource().getServer().getPlayerList()
                                                        .getPlayerByName(StringArgumentType.getString(ctx, "target"))))))
                        .then(Commands.literal("join")
                                .then(Commands.argument("party_id", StringArgumentType.word())
                                        .executes(ctx -> partyJoin(player(ctx.getSource()),
                                                StringArgumentType.getString(ctx, "party_id")))))
                        .then(Commands.literal("accept").executes(ctx -> partyAccept(player(ctx.getSource()))))
                        .then(Commands.literal("leave").executes(ctx -> partyLeave(player(ctx.getSource()))))
                        .then(Commands.literal("disband").executes(ctx -> partyDisband(player(ctx.getSource()))))
                        .then(Commands.literal("info").executes(ctx -> partyInfo(player(ctx.getSource()))))
                )
                .then(Commands.literal("givegold")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> giveGold(player(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "amount")))))
                .then(Commands.literal("addexp")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> addExp(player(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "amount")))))
                .then(Commands.literal("complete")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("quest_id", StringArgumentType.word())
                                .executes(ctx -> complete(player(ctx.getSource()), StringArgumentType.getString(ctx, "quest_id")))))
                .then(Commands.literal("abandon")
                        .then(Commands.argument("quest_id", StringArgumentType.word())
                                .executes(ctx -> abandon(player(ctx.getSource()), StringArgumentType.getString(ctx, "quest_id")))))
                .then(Commands.literal("reset")
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> reset(player(ctx.getSource()))))
                .then(Commands.literal("setrep")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> setReputation(player(ctx.getSource()),
                                        IntegerArgumentType.getInteger(ctx, "amount")))))
                .then(Commands.literal("spawnnpc")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("role", StringArgumentType.word())
                                .executes(ctx -> spawnNpc(player(ctx.getSource()),
                                        StringArgumentType.getString(ctx, "role")))))
                .then(Commands.literal("debug").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("chapter")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> debugChapter(player(ctx.getSource()),
                                                StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("event")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> debugEvent(player(ctx.getSource()),
                                                StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("npc")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> debugNpc(player(ctx.getSource()),
                                                StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("guild").executes(ctx -> debugGuild(player(ctx.getSource()))))
                        .then(Commands.literal("reset").executes(ctx -> debugReset(player(ctx.getSource()))))
                )
        );
    }

    private static ServerPlayer player(CommandSourceStack source) throws CommandSyntaxException {
        return source.getPlayerOrException();
    }

    private static int register(ServerPlayer player) {
        return QuestManager.register(player) ? 1 : 0;
    }

    private static int info(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return 0;
        }
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.header").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.registered",
                data.isRegistered()
                        ? Component.translatable("msg.adventurersguild.yes")
                        : Component.translatable("msg.adventurersguild.no")));
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.level", data.getLevel()));
        String expText = data.getLevel() >= LevelData.MAX_LEVEL
                ? "MAX"
                : data.getExperience() + " / " + data.getExpForNextLevel();
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.exp", expText));
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.gold", data.getGold()));
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.active",
                data.getActiveQuestCount(), AdventurerData.MAX_ACTIVE_QUESTS));
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.completed", data.getCompletedQuestCount()));
        for (QuestProgress progress : data.getActiveQuests()) {
            Quest quest = QuestRegistry.get(progress.getQuestId());
            player.sendSystemMessage(Component.translatable("cmd.adventurersguild.info.quest_line",
                    quest != null ? Component.translatable(quest.getTitleKey()) : Component.literal(progress.getQuestId()),
                    progress.getProgress(),
                    quest != null ? quest.getAmount() : 0,
                    progress.getStatus().name()));
        }
        return 1;
    }

    private static int openHall(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.hall(player));
        return 1;
    }

    private static int openMyQuests(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.myQuests(player));
        return 1;
    }

    private static int openAdventurer(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.adventurer(player));
        return 1;
    }

    private static int openShop(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.shop(player));
        return 1;
    }

    private static int openChains(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.chains(player));
        return 1;
    }

    private static int openMain(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.main(player));
        return 1;
    }

    private static int openChronicle(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player,
                com.adventurersguild.network.ChronicleUpdatePacket.forPlayer(player, true));
        return 1;
    }

    private static int refresh(ServerPlayer player) {
        return QuestManager.refreshBoard(player) >= 0 ? 1 : 0;
    }

    private static int guildLocate(ServerPlayer player) {
        GuildWorldData worldData = GuildWorldData.get(player.serverLevel());
        if (!worldData.isGuildGenerated()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.guild_not_found"));
            return 0;
        }
        net.minecraft.core.BlockPos pos = worldData.getGuildPosition();
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.guild_located",
                pos.getX(), pos.getY(), pos.getZ(), worldData.getGuildDimension()));
        return 1;
    }

    private static int partyCreate(ServerPlayer player, String name) {
        String id = PartyManager.create(player, name);
        return id != null ? 1 : 0;
    }

    private static int partyInvite(ServerPlayer player, ServerPlayer target) {
        if (target == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_target_offline"));
            return 0;
        }
        return PartyManager.invite(player, target) ? 1 : 0;
    }

    private static int partyJoin(ServerPlayer player, String partyId) {
        return PartyManager.join(player, partyId) ? 1 : 0;
    }

    private static int partyAccept(ServerPlayer player) {
        return PartyManager.accept(player) ? 1 : 0;
    }

    private static int partyLeave(ServerPlayer player) {
        return PartyManager.leave(player) ? 1 : 0;
    }

    private static int partyDisband(ServerPlayer player) {
        return PartyManager.disband(player) ? 1 : 0;
    }

    private static int partyInfo(ServerPlayer player) {
        PartyManager.info(player);
        return 1;
    }

    private static int setReputation(ServerPlayer player, int amount) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return 0;
        }
        data.setReputation(amount);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.reputation_set", amount));
        sync(player);
        return 1;
    }

    private static int spawnNpc(ServerPlayer player, String role) {
        return GuildNpcHandler.spawnNpc(player, role) ? 1 : 0;
    }

    private static int debugChapter(ServerPlayer player, String chapterId) {
        Chapter chapter = ChapterRegistry.get(chapterId);
        if (chapter == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.debug_missing", chapterId));
            return 0;
        }
        UnlockState unlocks = AdventurerCapability.getUnlockState(player);
        unlocks.unlock("chapter." + chapterId);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.debug_chapter", chapterId));
        sync(player);
        return 1;
    }

    private static int debugEvent(ServerPlayer player, String eventId) {
        ChronicleManager.recordEvent(player, eventId);
        return 1;
    }

    private static int debugNpc(ServerPlayer player, String role) {
        GuildNpcManager.spawnNpcAt(player.serverLevel(), player.blockPosition(), role);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.debug_npc", role));
        return 1;
    }

    private static int debugGuild(ServerPlayer player) {
        GuildWorldData worldData = GuildWorldData.get(player.serverLevel());
        if (!worldData.isGuildGenerated()) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.guild_not_found"));
            return 0;
        }
        net.minecraft.core.BlockPos pos = worldData.getGuildPosition();
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.debug_guild",
                pos.getX(), pos.getY(), pos.getZ(), worldData.getGuildDimension(),
                worldData.areNpcsSpawned() ? "true" : "false",
                worldData.getWorldEvents().size(), worldData.getParties().size()));
        return 1;
    }

    private static int debugReset(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return 0;
        }
        data.reset();
        GuildWorldData.get(player.serverLevel()).clearGuild();
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.debug_reset"));
        sync(player);
        return 1;
    }

    private static int giveGold(ServerPlayer player, int amount) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || !data.addGold(amount)) {
            return 0;
        }
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.gold_added", amount, data.getGold())
                .withStyle(ChatFormatting.GOLD));
        sync(player);
        return 1;
    }

    private static int addExp(ServerPlayer player, int amount) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return 0;
        }
        int levels = data.addExperience(amount);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.exp_added", amount, data.getExperience())
                .withStyle(ChatFormatting.AQUA));
        if (levels > 0) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.level_gained", levels, data.getLevel())
                    .withStyle(ChatFormatting.YELLOW));
        }
        sync(player);
        return 1;
    }

    private static int complete(ServerPlayer player, String questId) {
        return QuestManager.forceComplete(player, questId) ? 1 : 0;
    }

    private static int abandon(ServerPlayer player, String questId) {
        return QuestManager.abandonQuest(player, questId) ? 1 : 0;
    }

    private static int reset(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return 0;
        }
        data.reset();
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.data_reset"));
        sync(player);
        return 1;
    }

    private static void sync(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.update(player));
    }
}
