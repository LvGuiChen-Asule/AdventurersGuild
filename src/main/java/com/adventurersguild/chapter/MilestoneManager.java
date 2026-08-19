package com.adventurersguild.chapter;

import net.minecraft.server.level.ServerPlayer;

/**
 * TASK-013: milestone management. The milestone events themselves are recorded
 * by ChronicleEvents; this manager reacts to them and to quest completions to
 * drive chapter progression.
 */
public final class MilestoneManager {
    private MilestoneManager() {}

    /** Called by ChronicleManager whenever a player event is recorded. */
    public static void onEventRecorded(ServerPlayer player, String eventId) {
        ChapterManager.onEventRecorded(player, eventId);
    }

    /** Called when a quest is completed (chapter quest progression). */
    public static void onQuestCompleted(ServerPlayer player) {
        ChapterManager.onQuestCompleted(player);
    }
}
