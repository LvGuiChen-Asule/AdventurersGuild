package com.adventurersguild.quest;

import com.adventurersguild.player.AdventurerData;

import java.util.List;

/**
 * TASK-002: typed view of the quest-related player state
 * (active quests, completed history, counters, slot limit).
 */
public class QuestState {
    private final AdventurerData data;

    public QuestState(AdventurerData data) {
        this.data = data;
    }

    public List<QuestProgress> getActiveQuests() { return data.getActiveQuests(); }
    public int getActiveQuestCount() { return data.getActiveQuestCount(); }
    public boolean canAcceptMore() { return data.hasFreeQuestSlot(); }
    public boolean isActive(String questId) { return data.hasActiveQuest(questId); }
    public boolean isCompleted(String questId) { return data.isQuestCompleted(questId); }
    public int getCompletedCount() { return data.getCompletedQuestCount(); }
    public int getAbandonedCount() { return data.getAbandonedQuestCount(); }
    public int getMaxActiveQuests() { return AdventurerData.MAX_ACTIVE_QUESTS; }
}
