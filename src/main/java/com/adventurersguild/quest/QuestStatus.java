package com.adventurersguild.quest;

import java.util.Locale;

/**
 * Quest lifecycle states.
 * V0.1 actually uses: AVAILABLE, ACCEPTED, IN_PROGRESS, COMPLETED, ABANDONED.
 * FAILED is reserved for future versions.
 */
public enum QuestStatus {
    AVAILABLE,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    ABANDONED;

    public static QuestStatus byName(String name) {
        if (name == null || name.isBlank()) {
            return AVAILABLE;
        }
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "AVAILABLE" -> AVAILABLE;
            case "ACCEPTED" -> ACCEPTED;
            case "IN_PROGRESS" -> IN_PROGRESS;
            case "COMPLETED" -> COMPLETED;
            case "FAILED" -> FAILED;
            case "ABANDONED" -> ABANDONED;
            default -> throw new IllegalArgumentException("Unknown quest status: " + name);
        };
    }
}
