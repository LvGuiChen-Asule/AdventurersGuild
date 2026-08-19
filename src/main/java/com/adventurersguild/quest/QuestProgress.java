package com.adventurersguild.quest;

import net.minecraft.nbt.CompoundTag;

/** Per-player, per-quest progress for an accepted quest. */
public class QuestProgress {
    private static final String TAG_QUEST_ID = "questId";
    private static final String TAG_STATUS = "status";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_ACCEPTED_AT = "acceptedAtTick";

    private final String questId;
    private QuestStatus status;
    private int progress;
    private long acceptedAtTick;

    public QuestProgress(String questId, long acceptedAtTick) {
        this.questId = questId;
        this.status = QuestStatus.ACCEPTED;
        this.progress = 0;
        this.acceptedAtTick = acceptedAtTick;
    }

    public String getQuestId() { return questId; }
    public QuestStatus getStatus() { return status; }
    public int getProgress() { return progress; }
    public long getAcceptedAtTick() { return acceptedAtTick; }

    public void setStatus(QuestStatus status) { this.status = status; }
    public void setProgress(int progress) { this.progress = Math.max(0, progress); }
    public void addProgress(int amount) { this.progress += Math.max(0, amount); }

    /** ACCEPTED -> IN_PROGRESS once the player starts making progress. */
    public void markInProgress() {
        if (status == QuestStatus.ACCEPTED) {
            status = QuestStatus.IN_PROGRESS;
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_QUEST_ID, questId);
        tag.putString(TAG_STATUS, status.name());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putLong(TAG_ACCEPTED_AT, acceptedAtTick);
        return tag;
    }

    public static QuestProgress load(CompoundTag tag) {
        QuestProgress progress = new QuestProgress(tag.getString(TAG_QUEST_ID), tag.getLong(TAG_ACCEPTED_AT));
        progress.status = QuestStatus.byName(tag.getString(TAG_STATUS));
        progress.progress = Math.max(0, tag.getInt(TAG_PROGRESS));
        return progress;
    }
}
