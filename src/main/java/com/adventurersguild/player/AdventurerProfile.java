package com.adventurersguild.player;

/**
 * TASK-002: typed view of the base adventurer stats (registration / level /
 * EXP / gold / reputation). Backed by the persisted {@link AdventurerData}.
 */
public class AdventurerProfile {
    private final AdventurerData data;

    public AdventurerProfile(AdventurerData data) {
        this.data = data;
    }

    public boolean isRegistered() { return data.isRegistered(); }
    public String getName() { return data.getPlayerName(); }
    public int getLevel() { return data.getLevel(); }
    public int getExperience() { return data.getExperience(); }
    public long getGold() { return data.getGold(); }
    public int getReputation() { return data.getReputation(); }
    public int getReputationTier() { return ReputationData.getTier(data.getReputation()); }
    public long getFirstRegisteredAt() { return data.getFirstRegisteredAt(); }

    public String getLevelTitleKey() {
        return LevelData.getTitleKey(data.getLevel());
    }

    public String getReputationTierKey() {
        return ReputationData.getTierKey(ReputationData.getTier(data.getReputation()));
    }
}
