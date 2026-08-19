package com.adventurersguild.data;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.economy.Shop;
import com.adventurersguild.economy.ShopItem;
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

/** Loads shops from data/adventurersguild/shops/*.json (V0.5). */
public final class ShopRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, Shop> SHOPS = new LinkedHashMap<>();

    private ShopRegistry() {}

    public static Map<String, Shop> all() {
        return Collections.unmodifiableMap(SHOPS);
    }

    public static List<Shop> list() {
        return List.copyOf(SHOPS.values());
    }

    public static Shop get(String id) {
        return SHOPS.get(id);
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "shops");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, Shop> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject root = entry.getValue().getAsJsonObject();
                    String id = root.has("id") ? root.get("id").getAsString() : entry.getKey().getPath();
                    String titleKey = root.has("title") ? root.get("title").getAsString() : "shop.adventurersguild." + id + ".title";
                    String category = root.has("category") ? root.get("category").getAsString() : "";
                    List<ShopItem> items = new ArrayList<>();
                    JsonArray itemsArray = root.getAsJsonArray("items");
                    if (itemsArray != null) {
                        for (JsonElement element : itemsArray) {
                            JsonObject obj = element.getAsJsonObject();
                            items.add(new ShopItem(
                                    obj.get("item").getAsString(),
                                    obj.has("count") ? obj.get("count").getAsInt() : 1,
                                    obj.get("price").getAsInt(),
                                    obj.has("min_level") ? obj.get("min_level").getAsInt() : 1
                            ));
                        }
                    }
                    loaded.put(id, new Shop(id, titleKey, category, items));
                } catch (Exception e) {
                    AdventurersGuild.LOGGER.error("Failed to load shop '{}'", entry.getKey(), e);
                }
            }
            SHOPS.clear();
            SHOPS.putAll(loaded);
            AdventurersGuild.LOGGER.info("[Adventurer's Guild] Loaded {} shop(s)", loaded.size());
        }
    }
}
