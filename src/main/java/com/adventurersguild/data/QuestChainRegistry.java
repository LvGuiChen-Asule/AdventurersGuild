package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.quest.QuestChain;
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

/** Loads quest chains from data/adventurersguild/quest_chains/*.json (V0.8). */
public final class QuestChainRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, QuestChain> CHAINS = new LinkedHashMap<>();

    private QuestChainRegistry() {}

    public static Map<String, QuestChain> all() {
        return Collections.unmodifiableMap(CHAINS);
    }

    public static List<QuestChain> list() {
        return List.copyOf(CHAINS.values());
    }

    public static QuestChain get(String id) {
        return CHAINS.get(id);
    }

    public static QuestChain getByQuest(String questId) {
        for (QuestChain chain : CHAINS.values()) {
            for (QuestChain.Step step : chain.getSteps()) {
                if (step.getQuestId().equals(questId)) {
                    return chain;
                }
            }
        }
        return null;
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "quest_chains");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, QuestChain> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject root = entry.getValue().getAsJsonObject();
                    String id = root.has("id") ? root.get("id").getAsString() : entry.getKey().getPath();
                    String titleKey = root.has("title") ? root.get("title").getAsString() : "chain.adventurersguild." + id + ".title";
                    String descriptionKey = root.has("description") ? root.get("description").getAsString() : "chain.adventurersguild." + id + ".desc";
                    JsonArray stepsArray = root.getAsJsonArray("steps");
                    List<QuestChain.Step> steps = new ArrayList<>();
                    if (stepsArray != null) {
                        int index = 0;
                        for (JsonElement element : stepsArray) {
                            JsonObject obj = element.getAsJsonObject();
                            String questId = obj.get("quest_id").getAsString();
                            String unlockType = "quest";
                            int unlockValue = 0;
                            if (obj.has("unlock")) {
                                JsonObject unlock = obj.getAsJsonObject("unlock");
                                unlockType = unlock.has("type") ? unlock.get("type").getAsString() : "quest";
                                unlockValue = unlock.has("value") ? unlock.get("value").getAsInt() : 0;
                            }
                            steps.add(new QuestChain.Step(index, questId, unlockType, unlockValue));
                            index++;
                        }
                    }
                    loaded.put(id, new QuestChain(id, titleKey, descriptionKey, steps));
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load quest chain '{}'", entry.getKey(), e);
                }
            }
            CHAINS.clear();
            CHAINS.putAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} quest chain(s)", loaded.size());
        }
    }
}
