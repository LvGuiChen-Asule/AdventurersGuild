package com.adventurersguild.chapter;

import com.adventurersguild.AdventurersGuild;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads chapters from data/adventurersguild/chapters/*.json (TASK-013). */
public final class ChapterRegistry {
    public static final String DEFAULT_CHAPTER_ID = "chapter_0";

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, Chapter> CHAPTERS = new LinkedHashMap<>();

    private ChapterRegistry() {}

    public static List<Chapter> list() {
        return List.copyOf(CHAPTERS.values());
    }

    public static Map<String, Chapter> all() {
        return Collections.unmodifiableMap(CHAPTERS);
    }

    public static Chapter get(String id) {
        return CHAPTERS.get(id);
    }

    /** Returns the chapter that owns the given quest, or null. */
    public static Chapter getChapterForQuest(String questId) {
        for (Chapter chapter : CHAPTERS.values()) {
            if (chapter.getQuestIds().contains(questId)) {
                return chapter;
            }
        }
        return null;
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "chapters");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, Chapter> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject root = entry.getValue().getAsJsonObject();
                    String id = root.has("id") ? root.get("id").getAsString() : entry.getKey().getPath();
                    String titleKey = root.has("title") ? root.get("title").getAsString()
                            : "chapter.adventurersguild." + id + ".title";
                    String descriptionKey = root.has("description") ? root.get("description").getAsString()
                            : "chapter.adventurersguild." + id + ".desc";
                    String unlockType = "event";
                    String unlockValue = "";
                    if (root.has("unlock")) {
                        JsonObject unlock = root.getAsJsonObject("unlock");
                        unlockType = unlock.has("type") ? unlock.get("type").getAsString() : "event";
                        unlockValue = unlock.has("value") ? unlock.get("value").getAsString() : "";
                    }
                    List<String> quests = new ArrayList<>();
                    JsonArray questsArray = root.getAsJsonArray("quests");
                    if (questsArray != null) {
                        for (JsonElement element : questsArray) {
                            quests.add(element.getAsString());
                        }
                    }
                    loaded.put(id, new Chapter(id, titleKey, descriptionKey, unlockType, unlockValue, quests));
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load chapter '{}'", entry.getKey(), e);
                }
            }
            CHAPTERS.clear();
            CHAPTERS.putAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} chapter(s)", loaded.size());
        }
    }
}
