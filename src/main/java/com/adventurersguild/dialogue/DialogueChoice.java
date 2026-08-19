package com.adventurersguild.dialogue;

import java.util.List;

/** One selectable dialogue choice (TASK-010). */
public class DialogueChoice {
    private final String textKey;
    private final List<DialogueCondition> conditions;
    private final List<DialogueAction> actions;
    private final String next;

    public DialogueChoice(String textKey, List<DialogueCondition> conditions,
                          List<DialogueAction> actions, String next) {
        this.textKey = textKey;
        this.conditions = conditions;
        this.actions = actions;
        this.next = next == null || next.isBlank() ? "close" : next;
    }

    public String getTextKey() { return textKey; }
    public List<DialogueCondition> getConditions() { return conditions; }
    public List<DialogueAction> getActions() { return actions; }
    public String getNext() { return next; }
}
