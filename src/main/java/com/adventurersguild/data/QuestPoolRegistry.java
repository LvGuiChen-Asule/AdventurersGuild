package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.quest.QuestPoolEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Loads daily quest pool entries from data/adventurersguild/quest_pools/*.json.
 * All pools are aggregated; daily generation picks weighted entries per quality slot.
 */
public final class QuestPoolRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private static final List<QuestPoolEntry> ENTRIES = new ArrayList<>();

    private QuestPoolRegistry() {}

    public static List<QuestPoolEntry> list() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "quest_pools");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            List<QuestPoolEntry> loaded = new ArrayList<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject root = entry.getValue().getAsJsonObject();
                    JsonArray poolEntries = root.getAsJsonArray("entries");
                    if (poolEntries == null) {
                        continue;
                    }
                    for (JsonElement element : poolEntries) {
                        JsonObject obj = element.getAsJsonObject();
                        String questId = obj.get("quest_id").getAsString();
                        int weight = obj.has("weight") ? obj.get("weight").getAsInt() : 1;
                        int minLevel = obj.has("min_level") ? obj.get("min_level").getAsInt() : 1;
                        int maxLevel = obj.has("max_level") ? obj.get("max_level").getAsInt() : 6;
                        loaded.add(new QuestPoolEntry(questId, weight, minLevel, maxLevel));
                    }
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load quest pool '{}'", entry.getKey(), e);
                }
            }
            ENTRIES.clear();
            ENTRIES.addAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} quest pool entry(ies)", loaded.size());
        }
    }
}
