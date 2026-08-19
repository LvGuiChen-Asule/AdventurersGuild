package com.adventurersguild.economy;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/** A shop definition loaded from data/adventurersguild/shops/*.json (V0.5). */
public class Shop {
    private final String id;
    private final String titleKey;
    private final String category;
    private final List<ShopItem> items;

    public Shop(String id, String titleKey, String category, List<ShopItem> items) {
        this.id = id;
        this.titleKey = titleKey;
        this.category = category;
        this.items = items;
    }

    public String getId() { return id; }
    public String getTitleKey() { return titleKey; }
    public String getCategory() { return category; }
    public List<ShopItem> getItems() { return items; }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(titleKey);
        buf.writeUtf(category);
        buf.writeCollection(items, (b, item) -> {
            b.writeUtf(item.getItemId());
            b.writeVarInt(item.getCount());
            b.writeVarInt(item.getPrice());
            b.writeVarInt(item.getMinLevel());
        });
    }

    public static Shop decode(FriendlyByteBuf buf) {
        String id = buf.readUtf(128);
        String titleKey = buf.readUtf(256);
        String category = buf.readUtf(64);
        List<ShopItem> items = new ArrayList<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            items.add(new ShopItem(buf.readUtf(128), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        }
        return new Shop(id, titleKey, category, items);
    }
}
