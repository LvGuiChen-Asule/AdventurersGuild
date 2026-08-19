package com.adventurersguild.player;

import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestProgress;
import com.adventurersguild.quest.QuestStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Server-authoritative adventurer data, persisted with the player through a capability.
 * V0.1 fields: registered, level, experience, gold, activeQuestIds, completedQuestCount.
 */
public class AdventurerData {
    public static final int MAX_ACTIVE_QUESTS = 3;
    public static final long MAX_GOLD = 1_000_000_000L;

    private static final String TAG_REGISTERED = "registered";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_EXPERIENCE = "experience";
    private static final String TAG_GOLD = "gold";
    private static final String TAG_ACTIVE_QUESTS = "activeQuests";
    private static final String TAG_COMPLETED_COUNT = "completedQuestCount";
    private static final String TAG_REPUTATION = "reputation";
    private static final String TAG_REFRESH_COUNT = "dailyRefreshCount";
    private static final String TAG_REFRESH_DAY = "lastRefreshDay";
    private static final String TAG_ABANDONED_COUNT = "abandonedQuestCount";
    private static final String TAG_COMPLETED_QUEST_IDS = "completedQuestIds";
    private static final String TAG_CHAIN_PROGRESS = "chainProgress";
    private static final String TAG_PLAYER_NAME = "playerName";
    private static final String TAG_FIRST_REGISTERED_AT = "firstRegisteredAt";
    private static final String TAG_CHRONICLE_EVENTS = "chronicleEvents";
    private static final String TAG_LORE_DISCOVERED = "loreDiscovered";
    private static final String TAG_COUNTERS = "counters";
    private static final String TAG_UNLOCKS = "unlocks";
    private static final String TAG_PARTY_ID = "partyId";

    private boolean registered;
    private String playerName = "";
    private long firstRegisteredAt;
    private int level = 1;
    private int experience;
    private long gold;
    private final List<QuestProgress> activeQuests = new ArrayList<>();
    private int completedQuestCount;
    private int abandonedQuestCount;
    private int reputation;
    private int dailyRefreshCount;
    private long lastRefreshDay = -1;
    private final List<String> completedQuestIds = new ArrayList<>();
    private final Map<String, Integer> chainProgress = new HashMap<>();
    private final Set<String> chronicleEvents = new LinkedHashSet<>();
    private final Set<String> loreDiscovered = new LinkedHashSet<>();
    private final Map<String, Integer> counters = new LinkedHashMap<>();
    private final Set<String> unlocks = new LinkedHashSet<>();
    private String partyId;

    // ---------- registration ----------

    public boolean isRegistered() { return registered; }

    public void register() { registered = true; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName == null ? "" : playerName; }

    public long getFirstRegisteredAt() { return firstRegisteredAt; }
    public void setFirstRegisteredAt(long tick) { this.firstRegisteredAt = tick; }

    // ---------- gold ----------

    public long getGold() { return gold; }

    /** Adds gold. Returns false for non-positive amounts (anti-cheat guard). */
    public boolean addGold(long amount) {
        if (amount <= 0) {
            return false;
        }
        gold = Math.min(MAX_GOLD, gold + amount);
        return true;
    }

    /** Spends gold. Returns false when the amount is invalid or unaffordable. */
    public boolean spendGold(long amount) {
        if (amount <= 0 || gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    // ---------- level & experience ----------

    public int getLevel() { return level; }
    public int getExperience() { return experience; }

    /** Adds EXP and applies level-ups. Returns the number of levels gained. */
    public int addExperience(int amount) {
        if (amount <= 0) {
            return 0;
        }
        experience += amount;
        int gained = 0;
        while (level < LevelData.MAX_LEVEL && experience >= LevelData.getExpForLevel(level + 1)) {
            level++;
            gained++;
        }
        return gained;
    }

    /** Total EXP needed for the next level, or -1 at max level. */
    public int getExpForNextLevel() {
        return level >= LevelData.MAX_LEVEL ? -1 : LevelData.getExpForLevel(level + 1);
    }

    /** EXP earned within the current level (for progress display). */
    public int getExpIntoCurrentLevel() {
        if (level <= 1) {
            return experience;
        }
        return Math.max(0, experience - LevelData.getExpForLevel(level));
    }

    // ---------- quests ----------

    public List<QuestProgress> getActiveQuests() { return activeQuests; }

    public QuestProgress getActiveQuest(String questId) {
        for (QuestProgress progress : activeQuests) {
            if (progress.getQuestId().equals(questId)) {
                return progress;
            }
        }
        return null;
    }

    public boolean hasActiveQuest(String questId) { return getActiveQuest(questId) != null; }

    public boolean hasFreeQuestSlot() { return activeQuests.size() < MAX_ACTIVE_QUESTS; }

    public int getActiveQuestCount() { return activeQuests.size(); }

    /** Accepts a quest if the player is registered, has a free slot and is not already on it. */
    public boolean acceptQuest(Quest quest, long nowTick) {
        if (!registered || !hasFreeQuestSlot() || hasActiveQuest(quest.getId())) {
            return false;
        }
        activeQuests.add(new QuestProgress(quest.getId(), nowTick));
        return true;
    }

    /** Abandons an active quest. Returns true when it existed. */
    public boolean abandonQuest(String questId) {
        Iterator<QuestProgress> iterator = activeQuests.iterator();
        while (iterator.hasNext()) {
            QuestProgress progress = iterator.next();
            if (progress.getQuestId().equals(questId)) {
                progress.setStatus(QuestStatus.ABANDONED);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /** Removes a completed quest from the active list. */
    public boolean removeQuest(String questId) {
        return activeQuests.removeIf(p -> p.getQuestId().equals(questId));
    }

    public int getCompletedQuestCount() { return completedQuestCount; }

    public void incrementCompletedQuestCount() { completedQuestCount++; }

    // ---------- reputation (V0.2) ----------

    public int getReputation() { return reputation; }

    public void addReputation(int amount) {
        if (amount <= 0) {
            return;
        }
        int cap = ReputationData.getThresholdForTier(ReputationData.MAX_TIER);
        reputation = Math.min(cap, reputation + amount);
    }

    public void setReputation(int amount) {
        reputation = Math.max(0, amount);
    }

    // ---------- stats ----------

    public int getAbandonedQuestCount() { return abandonedQuestCount; }

    public void incrementAbandonedQuestCount() { abandonedQuestCount++; }

    public boolean isQuestCompleted(String questId) {
        return completedQuestIds.contains(questId);
    }

    public void recordQuestCompleted(String questId) {
        if (!completedQuestIds.contains(questId)) {
            completedQuestIds.add(questId);
        }
    }

    public List<String> getCompletedQuestIds() { return completedQuestIds; }

    // ---------- daily refresh (V0.4) ----------

    /**
     * Returns the gold cost of refreshing the daily board for the given world day.
     * First 3 refreshes per day are free, then 50/100/150/200 (capped at 200).
     */
    public int getRefreshCost(long day) {
        if (lastRefreshDay != day) {
            return 0;
        }
        int paidCount = Math.max(0, dailyRefreshCount - 3);
        if (paidCount <= 0) {
            return 0;
        }
        return Math.min(200, 50 * paidCount);
    }

    /** Records a refresh for the given day; returns the cost that was charged. */
    public int recordDailyRefresh(long day) {
        if (lastRefreshDay != day) {
            lastRefreshDay = day;
            dailyRefreshCount = 0;
        }
        int cost = getRefreshCost(day);
        dailyRefreshCount++;
        return cost;
    }

    /** Free refreshes remaining today (used by the UI). */
    public int getFreeRefreshesLeft(long day) {
        if (lastRefreshDay != day) {
            return 3;
        }
        return Math.max(0, 3 - dailyRefreshCount);
    }

    // ---------- quest chains (V0.8) ----------

    public int getChainProgress(String chainId) {
        return chainProgress.getOrDefault(chainId, 0);
    }

    public void setChainProgress(String chainId, int step) {
        chainProgress.put(chainId, Math.max(0, step));
    }

    public Map<String, Integer> getChainProgress() {
        return chainProgress;
    }

    // ---------- chronicle (TASK-002 / TASK-011) ----------

    public Set<String> getChronicleEvents() { return chronicleEvents; }
    public Set<String> getLoreDiscovered() { return loreDiscovered; }
    public Map<String, Integer> getCounters() { return counters; }

    // ---------- unlocks (TASK-002) ----------

    public Set<String> getUnlocks() { return unlocks; }

    // ---------- party reference (TASK-002 / TASK-019) ----------

    public String getPartyId() { return partyId; }
    public void setPartyId(String partyId) { this.partyId = partyId; }

    // ---------- lifecycle ----------

    public void copyFrom(AdventurerData other) {
        this.registered = other.registered;
        this.playerName = other.playerName;
        this.firstRegisteredAt = other.firstRegisteredAt;
        this.level = other.level;
        this.experience = other.experience;
        this.gold = other.gold;
        this.completedQuestCount = other.completedQuestCount;
        this.abandonedQuestCount = other.abandonedQuestCount;
        this.reputation = other.reputation;
        this.dailyRefreshCount = other.dailyRefreshCount;
        this.lastRefreshDay = other.lastRefreshDay;
        this.completedQuestIds.clear();
        this.completedQuestIds.addAll(other.completedQuestIds);
        this.chainProgress.clear();
        this.chainProgress.putAll(other.chainProgress);
        this.chronicleEvents.clear();
        this.chronicleEvents.addAll(other.chronicleEvents);
        this.loreDiscovered.clear();
        this.loreDiscovered.addAll(other.loreDiscovered);
        this.counters.clear();
        this.counters.putAll(other.counters);
        this.unlocks.clear();
        this.unlocks.addAll(other.unlocks);
        this.partyId = other.partyId;
        this.activeQuests.clear();
        for (QuestProgress progress : other.activeQuests) {
            QuestProgress copy = new QuestProgress(progress.getQuestId(), progress.getAcceptedAtTick());
            copy.setStatus(progress.getStatus());
            copy.setProgress(progress.getProgress());
            this.activeQuests.add(copy);
        }
    }

    public void reset() {
        registered = false;
        playerName = "";
        firstRegisteredAt = 0;
        level = 1;
        experience = 0;
        gold = 0;
        activeQuests.clear();
        completedQuestCount = 0;
        abandonedQuestCount = 0;
        reputation = 0;
        dailyRefreshCount = 0;
        lastRefreshDay = -1;
        completedQuestIds.clear();
        chainProgress.clear();
        chronicleEvents.clear();
        loreDiscovered.clear();
        counters.clear();
        unlocks.clear();
        partyId = null;
    }

    // ---------- persistence ----------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_REGISTERED, registered);
        tag.putString(TAG_PLAYER_NAME, playerName);
        tag.putLong(TAG_FIRST_REGISTERED_AT, firstRegisteredAt);
        tag.putInt(TAG_LEVEL, level);
        tag.putInt(TAG_EXPERIENCE, experience);
        tag.putLong(TAG_GOLD, gold);
        tag.putInt(TAG_COMPLETED_COUNT, completedQuestCount);
        tag.putInt(TAG_REPUTATION, reputation);
        tag.putInt(TAG_REFRESH_COUNT, dailyRefreshCount);
        tag.putLong(TAG_REFRESH_DAY, lastRefreshDay);
        tag.putInt(TAG_ABANDONED_COUNT, abandonedQuestCount);
        ListTag questList = new ListTag();
        for (QuestProgress progress : activeQuests) {
            questList.add(progress.save());
        }
        tag.put(TAG_ACTIVE_QUESTS, questList);
        ListTag completedList = new ListTag();
        for (String questId : completedQuestIds) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", questId);
            completedList.add(entry);
        }
        tag.put(TAG_COMPLETED_QUEST_IDS, completedList);
        ListTag chainList = new ListTag();
        for (Map.Entry<String, Integer> entry : chainProgress.entrySet()) {
            CompoundTag chainTag = new CompoundTag();
            chainTag.putString("id", entry.getKey());
            chainTag.putInt("step", entry.getValue());
            chainList.add(chainTag);
        }
        tag.put(TAG_CHAIN_PROGRESS, chainList);
        ListTag eventsList = new ListTag();
        for (String eventId : chronicleEvents) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", eventId);
            eventsList.add(entry);
        }
        tag.put(TAG_CHRONICLE_EVENTS, eventsList);
        ListTag loreList = new ListTag();
        for (String loreId : loreDiscovered) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", loreId);
            loreList.add(entry);
        }
        tag.put(TAG_LORE_DISCOVERED, loreList);
        CompoundTag countersTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : counters.entrySet()) {
            countersTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put(TAG_COUNTERS, countersTag);
        ListTag unlocksList = new ListTag();
        for (String unlock : unlocks) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", unlock);
            unlocksList.add(entry);
        }
        tag.put(TAG_UNLOCKS, unlocksList);
        if (partyId != null) {
            tag.putString(TAG_PARTY_ID, partyId);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        registered = tag.getBoolean(TAG_REGISTERED);
        playerName = tag.getString(TAG_PLAYER_NAME);
        firstRegisteredAt = tag.getLong(TAG_FIRST_REGISTERED_AT);
        level = Math.max(1, Math.min(LevelData.MAX_LEVEL, tag.getInt(TAG_LEVEL)));
        experience = Math.max(0, tag.getInt(TAG_EXPERIENCE));
        gold = Math.max(0, Math.min(MAX_GOLD, tag.getLong(TAG_GOLD)));
        completedQuestCount = Math.max(0, tag.getInt(TAG_COMPLETED_COUNT));
        abandonedQuestCount = Math.max(0, tag.getInt(TAG_ABANDONED_COUNT));
        reputation = Math.max(0, tag.getInt(TAG_REPUTATION));
        dailyRefreshCount = Math.max(0, tag.getInt(TAG_REFRESH_COUNT));
        lastRefreshDay = tag.getLong(TAG_REFRESH_DAY);
        activeQuests.clear();
        ListTag questList = tag.getList(TAG_ACTIVE_QUESTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < questList.size(); i++) {
            activeQuests.add(QuestProgress.load(questList.getCompound(i)));
        }
        completedQuestIds.clear();
        ListTag completedList = tag.getList(TAG_COMPLETED_QUEST_IDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < completedList.size(); i++) {
            completedQuestIds.add(completedList.getCompound(i).getString("id"));
        }
        chainProgress.clear();
        ListTag chainList = tag.getList(TAG_CHAIN_PROGRESS, Tag.TAG_COMPOUND);
        for (int i = 0; i < chainList.size(); i++) {
            CompoundTag chainTag = chainList.getCompound(i);
            chainProgress.put(chainTag.getString("id"), chainTag.getInt("step"));
        }
        chronicleEvents.clear();
        ListTag eventsList = tag.getList(TAG_CHRONICLE_EVENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < eventsList.size(); i++) {
            chronicleEvents.add(eventsList.getCompound(i).getString("id"));
        }
        loreDiscovered.clear();
        ListTag loreList = tag.getList(TAG_LORE_DISCOVERED, Tag.TAG_COMPOUND);
        for (int i = 0; i < loreList.size(); i++) {
            loreDiscovered.add(loreList.getCompound(i).getString("id"));
        }
        counters.clear();
        CompoundTag countersTag = tag.getCompound(TAG_COUNTERS);
        for (String key : countersTag.getAllKeys()) {
            counters.put(key, countersTag.getInt(key));
        }
        unlocks.clear();
        ListTag unlocksList = tag.getList(TAG_UNLOCKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < unlocksList.size(); i++) {
            unlocks.add(unlocksList.getCompound(i).getString("id"));
        }
        partyId = tag.contains(TAG_PARTY_ID) ? tag.getString(TAG_PARTY_ID) : null;
    }
}
