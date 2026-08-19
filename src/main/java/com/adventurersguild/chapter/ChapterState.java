package com.adventurersguild.chapter;

import com.adventurersguild.player.AdventurerData;
import com.adventurersguild.player.UnlockState;

/**
 * TASK-013: player-facing chapter state. Unlocks are stored in UnlockState
 * ("chapter.<id>"); this view computes the current chapter from them.
 */
public class ChapterState {
    private final AdventurerData data;

    public ChapterState(AdventurerData data) {
        this.data = data;
    }

    public boolean isUnlocked(Chapter chapter) {
        return data.getUnlocks().contains("chapter." + chapter.getId());
    }

    public String getCurrentChapterId() {
        String current = ChapterRegistry.DEFAULT_CHAPTER_ID;
        for (Chapter chapter : ChapterRegistry.list()) {
            if (isUnlocked(chapter)) {
                current = chapter.getId();
            }
        }
        return current;
    }

    public UnlockState asUnlockState() {
        return new UnlockState(data);
    }
}
