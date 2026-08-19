package com.adventurersguild.dialogue;

/** One server-executed dialogue action (TASK-010). */
public class DialogueAction {
    private final String type;
    private final String value;
    private final int amount;

    public DialogueAction(String type, String value, int amount) {
        this.type = type;
        this.value = value == null ? "" : value;
        this.amount = amount;
    }

    public String getType() { return type; }
    public String getValue() { return value; }
    public int getAmount() { return amount; }
}
