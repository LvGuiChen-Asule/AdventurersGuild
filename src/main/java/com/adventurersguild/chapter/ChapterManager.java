package com.adventurersguild.chapter;

import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.AdventurerProfile;
import com.adventurersguild.player.UnlockState;
import com.adventurersguild.quest.QuestState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * TASK-013: chapter progression manager.
 * Chapters unlock from recorded behavior (events / level / reputation) and
 * never block vanilla progression - the mod only tracks what the player did.
 */
public final class ChapterManager {
    private ChapterManager() {}

    public static boolean isUnlocked(ServerPlayer player, Chapter chapter) {
        UnlockState unlocks = AdventurerCapability.getUnlockState(player);
        if (unlocks == null) {
            return false;
        }
        if (unlocks.isUnlocked("chapter." + chapter.getId())) {
            return true;
        }
        return conditionMet(player, chapter);
    }

    private static boolean conditionMet(ServerPlayer player, Chapter chapter) {
        AdventurerProfile profile = AdventurerCapability.getProfile(player);
        if (profile == null) {
            return false;
        }
        return switch (chapter.getUnlockType()) {
            case "event" -> {
                if (chapter.getUnlockValue().isBlank()) {
                    yield true;
                }
                yield com.adventurersguild.chronicle.ChronicleManager.hasEvent(player, chapter.getUnlockValue());
            }
            case "level" -> profile.getLevel() >= parseInt(chapter.getUnlockValue());
            case "reputation" -> profile.getReputation() >= parseInt(chapter.getUnlockValue());
            default -> true;
        };
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /** Called after a player event is recorded: unlock any newly eligible chapters. */
    public static void onEventRecorded(ServerPlayer player, String eventId) {
        UnlockState unlocks = AdventurerCapability.getUnlockState(player);
        if (unlocks == null) {
            return;
        }
        boolean changed = false;
        for (Chapter chapter : ChapterRegistry.list()) {
            String key = "chapter." + chapter.getId();
            if (!unlocks.isUnlocked(key) && conditionMet(player, chapter)) {
                unlocks.unlock(key);
                changed = true;
                player.sendSystemMessage(Component.translatable(
                                "msg.adventurersguild.chapter_unlocked",
                                Component.translatable(chapter.getTitleKey()))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
        if (changed) {
            GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.update(player));
        }
    }

    /** Called after a quest completes: notify when a whole chapter is done. */
    public static void onQuestCompleted(ServerPlayer player) {
        QuestState questState = AdventurerCapability.getQuestState(player);
        UnlockState unlocks = AdventurerCapability.getUnlockState(player);
        if (questState == null || unlocks == null) {
            return;
        }
        for (Chapter chapter : ChapterRegistry.list()) {
            if (chapter.getQuestIds().isEmpty()
                    || !unlocks.isUnlocked("chapter." + chapter.getId())) {
                continue;
            }
            boolean allDone = true;
            for (String questId : chapter.getQuestIds()) {
                if (!questState.isCompleted(questId)) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                player.sendSystemMessage(Component.translatable(
                                "msg.adventurersguild.chapter_completed",
                                Component.translatable(chapter.getTitleKey()))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    /** Whether the chapter owning this quest is unlocked (quest accept gate). */
    public static boolean isChapterQuestUnlocked(ServerPlayer player, String questId) {
        Chapter chapter = ChapterRegistry.getChapterForQuest(questId);
        if (chapter == null) {
            return true;
        }
        return isUnlocked(player, chapter);
    }
}
