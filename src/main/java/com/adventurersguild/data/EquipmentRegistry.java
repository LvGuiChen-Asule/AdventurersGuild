package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.equipment.EquipmentData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads accessory definitions from data/adventurersguild/equipment/*.json (V0.6). */
public final class EquipmentRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, EquipmentData> BY_ITEM = new LinkedHashMap<>();

    private EquipmentRegistry() {}

    public static Map<String, EquipmentData> all() {
        return Collections.unmodifiableMap(BY_ITEM);
    }

    public static EquipmentData getByItem(Item item) {
        if (item == null) {
            return null;
        }
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        return key != null ? BY_ITEM.get(key.toString()) : null;
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "equipment");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, EquipmentData> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject root = entry.getValue().getAsJsonObject();
                    String id = root.has("id") ? root.get("id").getAsString() : entry.getKey().getPath();
                    String itemId = root.get("item").getAsString();
                    String slot = root.has("slot") ? root.get("slot").getAsString() : "charm";
                    String effect = root.get("effect").getAsString();
                    double value = root.has("value") ? root.get("value").getAsDouble() : 0.05;
                    String descriptionKey = root.has("description")
                            ? root.get("description").getAsString()
                            : "equipment.adventurersguild." + id + ".desc";
                    loaded.put(itemId, new EquipmentData(id, itemId, slot, effect, value, descriptionKey));
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load equipment '{}'", entry.getKey(), e);
                }
            }
            BY_ITEM.clear();
            BY_ITEM.putAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} equipment entry(ies)", loaded.size());
        }
    }
}
