package com.adventurersguild.economy;

/** One purchasable item in a shop (V0.5). */
public class ShopItem {
    private final String itemId;
    private final int count;
    private final int price;
    private final int minLevel;

    public ShopItem(String itemId, int count, int price, int minLevel) {
        this.itemId = itemId;
        this.count = Math.max(1, count);
        this.price = Math.max(0, price);
        this.minLevel = Math.max(1, minLevel);
    }

    public String getItemId() { return itemId; }
    public int getCount() { return count; }
    public int getPrice() { return price; }
    public int getMinLevel() { return minLevel; }
}
