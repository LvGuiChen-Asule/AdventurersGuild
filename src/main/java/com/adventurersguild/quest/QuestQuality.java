package com.adventurersguild.quest;

import java.util.Locale;

/**
 * Quest quality tiers with reward multipliers (V0.4+).
 * COMMON=1.0, UNCOMMON=1.25, RARE=2.0, EPIC=3.0, LEGENDARY=5.0
 */
public enum QuestQuality {
    COMMON(1.0f),
    UNCOMMON(1.25f),
    RARE(2.0f),
    EPIC(3.0f),
    LEGENDARY(5.0f);

    private final float rewardMultiplier;

    QuestQuality(float rewardMultiplier) {
        this.rewardMultiplier = rewardMultiplier;
    }

    public float getRewardMultiplier() {
        return rewardMultiplier;
    }

    public static QuestQuality byName(String name) {
        if (name == null || name.isBlank()) {
            return COMMON;
        }
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "COMMON" -> COMMON;
            case "UNCOMMON" -> UNCOMMON;
            case "RARE" -> RARE;
            case "EPIC" -> EPIC;
            case "LEGENDARY" -> LEGENDARY;
            default -> throw new IllegalArgumentException("Unknown quest quality: " + name);
        };
    }
}
