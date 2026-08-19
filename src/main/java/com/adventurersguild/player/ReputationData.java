package com.adventurersguild.player;

/**
 * Guild reputation tiers (V0.2).
 * 0=Stranger, 100=Newcomer, 300=Trusted, 800=Member, 1500=Elite, 3000=Celebrity
 */
public final class ReputationData {
    public static final int[] TIER_THRESHOLDS = { 0, 100, 300, 800, 1500, 3000 };
    public static final int MAX_TIER = TIER_THRESHOLDS.length - 1;

    private ReputationData() {}

    /** Returns the reputation tier index (0-based) for the given reputation. */
    public static int getTier(int reputation) {
        int tier = 0;
        for (int i = 0; i < TIER_THRESHOLDS.length; i++) {
            if (reputation >= TIER_THRESHOLDS[i]) {
                tier = i;
            } else {
                break;
            }
        }
        return tier;
    }

    public static int getThresholdForTier(int tier) {
        if (tier < 0) return 0;
        if (tier >= TIER_THRESHOLDS.length) return TIER_THRESHOLDS[TIER_THRESHOLDS.length - 1];
        return TIER_THRESHOLDS[tier];
    }

    /** Next tier threshold, or -1 at max tier. */
    public static int getNextThreshold(int reputation) {
        int tier = getTier(reputation);
        return tier >= MAX_TIER ? -1 : TIER_THRESHOLDS[tier + 1];
    }

    public static String getTierKey(int tier) {
        return "reputation.adventurersguild.tier." + tier;
    }
}
