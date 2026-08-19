package com.adventurersguild.quest;

import java.util.Locale;

/**
 * Quest types. V0.1: COLLECT / HUNT. V0.4 adds EXPLORE / SURVIVE.
 * V0.8 adds TRANSPORT / ELITE.
 */
public enum QuestType {
    COLLECT,
    HUNT,
    EXPLORE,
    SURVIVE,
    TRANSPORT,
    ELITE,
    INTERACT,
    MILESTONE,
    ACHIEVEMENT;

    public static QuestType byName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Quest type is missing");
        }
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "COLLECT" -> COLLECT;
            case "HUNT" -> HUNT;
            case "EXPLORE" -> EXPLORE;
            case "SURVIVE" -> SURVIVE;
            case "TRANSPORT" -> TRANSPORT;
            case "ELITE" -> ELITE;
            case "INTERACT" -> INTERACT;
            case "MILESTONE" -> MILESTONE;
            case "ACHIEVEMENT" -> ACHIEVEMENT;
            default -> throw new IllegalArgumentException("Unknown quest type: " + name);
        };
    }
}
