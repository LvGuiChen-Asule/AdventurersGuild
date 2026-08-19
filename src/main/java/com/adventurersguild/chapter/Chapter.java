package com.adventurersguild.chapter;

import java.util.ArrayList;
import java.util.List;

/** One main-story chapter (TASK-013). Weakly linear: unlock = recorded behavior. */
public class Chapter {
    private final String id;
    private final String titleKey;
    private final String descriptionKey;
    private final String unlockType;
    private final String unlockValue;
    private final List<String> questIds;

    public Chapter(String id, String titleKey, String descriptionKey,
                   String unlockType, String unlockValue, List<String> questIds) {
        this.id = id;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.unlockType = unlockType == null || unlockType.isBlank() ? "event" : unlockType;
        this.unlockValue = unlockValue == null ? "" : unlockValue;
        this.questIds = questIds == null ? new ArrayList<>() : questIds;
    }

    public String getId() { return id; }
    public String getTitleKey() { return titleKey; }
    public String getDescriptionKey() { return descriptionKey; }
    public String getUnlockType() { return unlockType; }
    public String getUnlockValue() { return unlockValue; }
    public List<String> getQuestIds() { return questIds; }
}
