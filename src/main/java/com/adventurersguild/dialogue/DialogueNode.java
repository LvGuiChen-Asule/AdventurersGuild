package com.adventurersguild.dialogue;

import java.util.List;

/** One dialogue node (TASK-010). */
public class DialogueNode {
    private final String id;
    private final String textKey;
    private final List<DialogueChoice> choices;

    public DialogueNode(String id, String textKey, List<DialogueChoice> choices) {
        this.id = id;
        this.textKey = textKey;
        this.choices = choices;
    }

    public String getId() { return id; }
    public String getTextKey() { return textKey; }
    public List<DialogueChoice> getChoices() { return choices; }
}
