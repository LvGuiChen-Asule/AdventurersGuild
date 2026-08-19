package com.adventurersguild.chronicle;

import com.adventurersguild.network.ChronicleUpdatePacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.chapter.MilestoneManager;
import com.adventurersguild.data.LoreRegistry;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.ChronicleState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * TASK-011: chronicle framework - records what the player has actually done.
 * Events are per-player (ChronicleState) and once=true by default; they are
 * queryable, persisted, synced and usable as dialogue conditions.
 */
public final class ChronicleManager {
    public static final String EVENT_GUILD_FOUND = "EVENT_GUILD_FOUND";
    public static final String EVENT_GUILD_REGISTER = "EVENT_GUILD_REGISTER";
    public static final String EVENT_FIRST_DEATH = "EVENT_FIRST_DEATH";
    public static final String EVENT_FIRST_NETHER = "EVENT_FIRST_NETHER";
    public static final String EVENT_NETHER_FORTRESS = "EVENT_NETHER_FORTRESS";
    public static final String EVENT_FIRST_ENDER_EYE = "EVENT_FIRST_ENDER_EYE";
    public static final String EVENT_STRONGHOLD_FOUND = "EVENT_STRONGHOLD_FOUND";
    public static final String EVENT_END_PORTAL_OPEN = "EVENT_END_PORTAL_OPEN";
    public static final String EVENT_FIRST_END = "EVENT_FIRST_END";
    public static final String EVENT_FIRST_DRAGON_ATTACK = "EVENT_FIRST_DRAGON_ATTACK";
    public static final String EVENT_DRAGON_DEATH = "EVENT_DRAGON_DEATH";
    public static final String EVENT_WITHER_SUMMON = "EVENT_WITHER_SUMMON";
    public static final String EVENT_WITHER_DEATH = "EVENT_WITHER_DEATH";
    public static final String EVENT_END_ISLAND = "EVENT_END_ISLAND";
    public static final String EVENT_100_QUESTS = "EVENT_100_QUESTS";

    private ChronicleManager() {}

    /** Records a player event exactly once and syncs the chronicle. */
    public static void recordEvent(ServerPlayer player, String eventId) {
        ChronicleState state = AdventurerCapability.getChronicleState(player);
        if (state == null || state.hasEvent(eventId)) {
            return;
        }
        state.recordEvent(eventId);
        String loreId = LoreRegistry.getLoreForEvent(eventId);
        if (loreId != null && !state.hasLore(loreId)) {
            state.discoverLore(loreId);
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.lore_discovered",
                    Component.translatable(LoreRegistry.get(loreId).getTitleKey())));
        }
        MilestoneManager.onEventRecorded(player, eventId);
        syncChronicle(player);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.event_recorded",
                Component.translatable("event.adventurersguild." + eventId)));
    }

    public static boolean hasEvent(ServerPlayer player, String eventId) {
        ChronicleState state = AdventurerCapability.getChronicleState(player);
        return state != null && state.hasEvent(eventId);
    }

    public static void syncChronicle(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, ChronicleUpdatePacket.forPlayer(player, false));
    }
}
