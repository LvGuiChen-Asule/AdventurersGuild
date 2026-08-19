package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads lore records from data/adventurersguild/lore/*.json (TASK-017). */
public final class LoreRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, LoreEntry> LORE = new LinkedHashMap<>();

    private LoreRegistry() {}

    public static Map<String, LoreEntry> all() {
        return Collections.unmodifiableMap(LORE);
    }

    public static LoreEntry get(String id) {
        return LORE.get(id);
    }

    /** Returns the lore auto-discovered when the given world event occurs. */
    public static String getLoreForEvent(String eventId) {
        for (LoreEntry entry : LORE.values()) {
            if (eventId.equals(entry.getUnlockEvent())) {
                return entry.getId();
            }
        }
        return null;
    }

    public static class LoreEntry {
        private final String id;
        private final String titleKey;
        private final String textKey;
        private final String unlockEvent;

        public LoreEntry(String id, String titleKey, String textKey, String unlockEvent) {
            this.id = id;
            this.titleKey = titleKey;
            this.textKey = textKey;
            this.unlockEvent = unlockEvent;
        }

        public String getId() { return id; }
        public String getTitleKey() { return titleKey; }
        public String getTextKey() { return textKey; }
        public String getUnlockEvent() { return unlockEvent; }
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "lore");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, LoreEntry> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject root = entry.getValue().getAsJsonObject();
                    String id = root.has("id") ? root.get("id").getAsString() : entry.getKey().getPath();
                    String titleKey = root.has("title") ? root.get("title").getAsString()
                            : "lore.adventurersguild." + id + ".title";
                    String textKey = root.has("text") ? root.get("text").getAsString()
                            : "lore.adventurersguild." + id + ".text";
                    String unlockEvent = root.has("unlock_event") ? root.get("unlock_event").getAsString() : "";
                    loaded.put(id, new LoreEntry(id, titleKey, textKey, unlockEvent));
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load lore '{}'", entry.getKey(), e);
                }
            }
            LORE.clear();
            LORE.putAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} lore record(s)", loaded.size());
        }
    }
}
