package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestPoolEntry;
import com.adventurersguild.quest.QuestQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server-global daily quest board (V0.4).
 * Each world day generates: 3 COMMON + 2 UNCOMMON + 1 RARE quests from the pool.
 */
public class DailyBoardManager {
    private static final String DATA_KEY = "adventurersguild_daily";
    private static final QuestQuality[] SLOT_QUALITIES = {
            QuestQuality.COMMON, QuestQuality.COMMON, QuestQuality.COMMON,
            QuestQuality.UNCOMMON, QuestQuality.UNCOMMON,
            QuestQuality.RARE
    };

    private DailyBoardManager() {}

    public static long currentDay(ServerLevel level) {
        return level.getDayTime() / 24000L;
    }

    public static List<String> getDailyQuestIds(ServerLevel level) {
        GuildDailyData data = level.getDataStorage().computeIfAbsent(
                GuildDailyData::load, GuildDailyData::new, DATA_KEY);
        long day = currentDay(level);
        if (data.day != day) {
            data.regenerate(day, level.getRandom());
        }
        return data.questIds;
    }

    public static boolean isOnBoard(ServerLevel level, String questId) {
        return getDailyQuestIds(level).contains(questId);
    }

    /** Re-rolls the board for the current day (used by paid refresh and dev command). */
    public static void reroll(ServerLevel level) {
        GuildDailyData data = level.getDataStorage().computeIfAbsent(
                GuildDailyData::load, GuildDailyData::new, DATA_KEY);
        data.regenerate(currentDay(level), level.getRandom());
    }

    private static String pickWeighted(List<QuestPoolEntry> candidates, RandomSource random) {
        int total = candidates.stream().mapToInt(QuestPoolEntry::getWeight).sum();
        if (total <= 0) {
            return candidates.isEmpty() ? null : candidates.get(0).getQuestId();
        }
        int roll = random.nextInt(total);
        int acc = 0;
        for (QuestPoolEntry entry : candidates) {
            acc += entry.getWeight();
            if (roll < acc) {
                return entry.getQuestId();
            }
        }
        return candidates.get(candidates.size() - 1).getQuestId();
    }

    public static class GuildDailyData extends SavedData {
        private long day = -1;
        private final List<String> questIds = new ArrayList<>();

        public GuildDailyData() {}

        public static GuildDailyData load(CompoundTag tag) {
            GuildDailyData data = new GuildDailyData();
            data.day = tag.getLong("day");
            ListTag list = tag.getList("quests", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                data.questIds.add(list.getString(i));
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putLong("day", day);
            ListTag list = new ListTag();
            for (String questId : questIds) {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", questId);
                list.add(entry);
            }
            tag.put("quests", list);
            return tag;
        }

        private void regenerate(long newDay, RandomSource random) {
            day = newDay;
            questIds.clear();
            List<QuestPoolEntry> pool = new ArrayList<>(QuestPoolRegistry.list());
            for (QuestQuality slotQuality : SLOT_QUALITIES) {
                List<QuestPoolEntry> candidates = new ArrayList<>();
                for (QuestPoolEntry entry : pool) {
                    Quest quest = QuestRegistry.get(entry.getQuestId());
                    if (quest == null) {
                        continue;
                    }
                    if (quest.getQuality() != slotQuality) {
                        continue;
                    }
                    candidates.add(entry);
                }
                String picked = pickWeighted(candidates, random);
                if (picked != null && !questIds.contains(picked)) {
                    questIds.add(picked);
                }
            }
            setDirty();
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Daily board regenerated for day {}: {}",
                    day, questIds);
        }
    }
}
