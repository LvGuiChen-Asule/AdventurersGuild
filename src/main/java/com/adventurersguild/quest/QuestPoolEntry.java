package com.adventurersguild.quest;

/** One weighted entry in the daily quest pool (V0.4). */
public class QuestPoolEntry {
    private final String questId;
    private final int weight;
    private final int minLevel;
    private final int maxLevel;

    public QuestPoolEntry(String questId, int weight, int minLevel, int maxLevel) {
        this.questId = questId;
        this.weight = Math.max(1, weight);
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public String getQuestId() { return questId; }
    public int getWeight() { return weight; }
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }
}
