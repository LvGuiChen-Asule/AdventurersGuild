package com.adventurersguild.player;

/**
 * Adventurer level curve (V0.1 base data, extended in V0.2 with titles/unlocks).
 * Total experience required to reach each level:
 * Lv1=0, Lv2=300, Lv3=900, Lv4=2000, Lv5=4000, Lv6=7500.
 */
public final class LevelData {
    public static final int MAX_LEVEL = 6;

    private static final int[] TOTAL_EXP_REQUIRED = { 0, 300, 900, 2000, 4000, 7500 };
    private static final String[] TITLE_KEYS = {
            "level.adventurersguild.title.1",
            "level.adventurersguild.title.2",
            "level.adventurersguild.title.3",
            "level.adventurersguild.title.4",
            "level.adventurersguild.title.5",
            "level.adventurersguild.title.6"
    };

    private LevelData() {}

    /** Total experience required to reach the given level (1-based). */
    public static int getExpForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        if (level >= MAX_LEVEL) {
            return TOTAL_EXP_REQUIRED[MAX_LEVEL - 1];
        }
        return TOTAL_EXP_REQUIRED[level - 1];
    }

    /** Level title language key (V0.2): rookie ... master adventurer. */
    public static String getTitleKey(int level) {
        if (level < 1) level = 1;
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        return TITLE_KEYS[level - 1];
    }

    /** Whether the given level unlocks content at the given unlock level. */
    public static boolean isUnlocked(int currentLevel, int requiredLevel) {
        return currentLevel >= requiredLevel;
    }
}
