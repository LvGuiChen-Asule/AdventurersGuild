package com.adventurersguild.player;

import java.util.Collections;
import java.util.Set;

/**
 * TASK-002: unlock state - chapters, endgame and content flags.
 * Server-authoritative; stored inside {@link AdventurerData} NBT.
 */
public class UnlockState {
    private final AdventurerData data;

    public UnlockState(AdventurerData data) {
        this.data = data;
    }

    public boolean isUnlocked(String key) {
        return data.getUnlocks().contains(key);
    }

    public void unlock(String key) {
        data.getUnlocks().add(key);
    }

    public Set<String> getUnlocks() {
        return Collections.unmodifiableSet(data.getUnlocks());
    }
}
