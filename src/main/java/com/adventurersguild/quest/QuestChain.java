package com.adventurersguild.quest;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/** A linear quest chain (V0.8): each step must be completed to unlock the next. */
public class QuestChain {
    private final String id;
    private final String titleKey;
    private final String descriptionKey;
    private final List<Step> steps;

    public QuestChain(String id, String titleKey, String descriptionKey, List<Step> steps) {
        this.id = id;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.steps = steps;
    }

    public String getId() { return id; }
    public String getTitleKey() { return titleKey; }
    public String getDescriptionKey() { return descriptionKey; }
    public List<Step> getSteps() { return steps; }

    public Step getStep(int index) {
        return index >= 0 && index < steps.size() ? steps.get(index) : null;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(titleKey);
        buf.writeUtf(descriptionKey);
        buf.writeCollection(steps, (b, step) -> step.encode(b));
    }

    public static QuestChain decode(FriendlyByteBuf buf) {
        String id = buf.readUtf(128);
        String titleKey = buf.readUtf(256);
        String descriptionKey = buf.readUtf(512);
        List<Step> steps = new ArrayList<>(buf.readList(Step::decode));
        return new QuestChain(id, titleKey, descriptionKey, steps);
    }

    public static class Step {
        private final int step;
        private final String questId;
        private final String unlockType;
        private final int unlockValue;

        public Step(int step, String questId, String unlockType, int unlockValue) {
            this.step = step;
            this.questId = questId;
            this.unlockType = unlockType == null || unlockType.isBlank() ? "quest" : unlockType;
            this.unlockValue = unlockValue;
        }

        public int getStep() { return step; }
        public String getQuestId() { return questId; }
        public String getUnlockType() { return unlockType; }
        public int getUnlockValue() { return unlockValue; }

        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(step);
            buf.writeUtf(questId);
            buf.writeUtf(unlockType);
            buf.writeVarInt(unlockValue);
        }

        public static Step decode(FriendlyByteBuf buf) {
            return new Step(buf.readVarInt(), buf.readUtf(128), buf.readUtf(32), buf.readVarInt());
        }
    }
}
