package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.dialogue.Dialogue;
import com.adventurersguild.dialogue.DialogueAction;
import com.adventurersguild.dialogue.DialogueChoice;
import com.adventurersguild.dialogue.DialogueCondition;
import com.adventurersguild.dialogue.DialogueNode;
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

/** Loads dialogues from data/adventurersguild/dialogues/*.json (TASK-010). */
public final class DialogueRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, Dialogue> DIALOGUES = new LinkedHashMap<>();

    private DialogueRegistry() {}

    public static Dialogue get(String id) {
        return DIALOGUES.get(id);
    }

    public static Dialogue getForNpc(String npcRole) {
        for (Dialogue dialogue : DIALOGUES.values()) {
            if (dialogue.getNpc().equals(npcRole)) {
                return dialogue;
            }
        }
        return null;
    }

    public static Map<String, Dialogue> all() {
        return Collections.unmodifiableMap(DIALOGUES);
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "dialogues");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, Dialogue> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    Dialogue dialogue = parse(entry.getKey(), entry.getValue().getAsJsonObject());
                    loaded.put(dialogue.getId(), dialogue);
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load dialogue '{}'", entry.getKey(), e);
                }
            }
            DIALOGUES.clear();
            DIALOGUES.putAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} dialogue(s)", loaded.size());
        }
    }

    private static Dialogue parse(ResourceLocation file, JsonObject root) {
        String id = root.has("id") ? root.get("id").getAsString() : file.getPath();
        String npc = root.get("npc").getAsString();
        String start = root.has("start") ? root.get("start").getAsString() : "start";
        Map<String, DialogueNode> nodes = Dialogue.newNodeMap();
        JsonArray nodesArray = root.getAsJsonArray("nodes");
        if (nodesArray != null) {
            for (JsonElement element : nodesArray) {
                JsonObject nodeObj = element.getAsJsonObject();
                String nodeId = nodeObj.get("id").getAsString();
                String textKey = nodeObj.get("text").getAsString();
                List<DialogueChoice> choices = new ArrayList<>();
                JsonArray choicesArray = nodeObj.getAsJsonArray("choices");
                if (choicesArray != null) {
                    for (JsonElement choiceElement : choicesArray) {
                        choices.add(parseChoice(choiceElement.getAsJsonObject()));
                    }
                }
                nodes.put(nodeId, new DialogueNode(nodeId, textKey, choices));
            }
        }
        return new Dialogue(id, npc, start, nodes);
    }

    private static DialogueChoice parseChoice(JsonObject obj) {
        String textKey = obj.get("text").getAsString();
        String next = obj.has("next") ? obj.get("next").getAsString() : "close";
        List<DialogueCondition> conditions = new ArrayList<>();
        JsonArray conditionsArray = obj.getAsJsonArray("conditions");
        if (conditionsArray != null) {
            for (JsonElement element : conditionsArray) {
                JsonObject condition = element.getAsJsonObject();
                conditions.add(new DialogueCondition(
                        condition.get("type").getAsString(),
                        condition.has("value") ? condition.get("value").getAsString() : "",
                        condition.has("not") && condition.get("not").getAsBoolean()));
            }
        }
        List<DialogueAction> actions = new ArrayList<>();
        JsonArray actionsArray = obj.getAsJsonArray("actions");
        if (actionsArray != null) {
            for (JsonElement element : actionsArray) {
                JsonObject action = element.getAsJsonObject();
                actions.add(new DialogueAction(
                        action.get("type").getAsString(),
                        action.has("value") ? action.get("value").getAsString() : "",
                        action.has("amount") ? action.get("amount").getAsInt() : 0));
            }
        }
        return new DialogueChoice(textKey, conditions, actions, next);
    }
}
