package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.quest.Quest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-side registry of quest definitions loaded from
 * data/adventurersguild/quests/*.json (datapack-reloadable).
 */
public final class QuestRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, Quest> QUESTS = new LinkedHashMap<>();

    private QuestRegistry() {}

    public static Map<String, Quest> all() {
        return Collections.unmodifiableMap(QUESTS);
    }

    public static List<Quest> list() {
        return List.copyOf(QUESTS.values());
    }

    public static Quest get(String id) {
        return QUESTS.get(id);
    }

    public static int size() {
        return QUESTS.size();
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "quests");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, Quest> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    Quest quest = Quest.fromJson(entry.getKey(), entry.getValue().getAsJsonObject());
                    loaded.put(quest.getId(), quest);
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load quest '{}'", entry.getKey(), e);
                }
            }
            QUESTS.clear();
            QUESTS.putAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} quest(s)", loaded.size());
        }
    }
}
