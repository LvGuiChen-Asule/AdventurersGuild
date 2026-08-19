package com.adventurersguild.dialogue;

import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.ChronicleState;
import com.adventurersguild.player.UnlockState;
import com.adventurersguild.quest.QuestState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Server-evaluated dialogue condition (TASK-010). */
public class DialogueCondition {
    private final String type;
    private final String value;
    private final boolean not;

    public DialogueCondition(String type, String value, boolean not) {
        this.type = type;
        this.value = value == null ? "" : value;
        this.not = not;
    }

    public String getType() { return type; }
    public String getValue() { return value; }
    public boolean isNot() { return not; }

    public boolean met(ServerPlayer player) {
        boolean base = baseMet(player);
        return not != base;
    }

    private boolean baseMet(ServerPlayer player) {
        QuestState questState = AdventurerCapability.getQuestState(player);
        ChronicleState chronicle = AdventurerCapability.getChronicleState(player);
        UnlockState unlocks = AdventurerCapability.getUnlockState(player);
        if (questState == null || chronicle == null || unlocks == null) {
            return false;
        }
        return switch (type) {
            case "chapter" -> unlocks.isUnlocked("chapter." + value);
            case "quest" -> questState.isCompleted(value);
            case "event" -> chronicle.hasEvent(value);
            case "lore" -> chronicle.hasLore(value);
            case "reputation" -> {
                int threshold = parseInt(value, 0);
                yield AdventurerCapability.getProfile(player) != null
                        && AdventurerCapability.getProfile(player).getReputation() >= threshold;
            }
            case "level" -> {
                int threshold = parseInt(value, 1);
                yield AdventurerCapability.getProfile(player) != null
                        && AdventurerCapability.getProfile(player).getLevel() >= threshold;
            }
            case "first_visit" -> chronicle.getCounter("visit." + value) <= 0;
            case "dimension" -> player.level().dimension().location()
                    .equals(ResourceLocation.tryParse(value));
            default -> true;
        };
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
