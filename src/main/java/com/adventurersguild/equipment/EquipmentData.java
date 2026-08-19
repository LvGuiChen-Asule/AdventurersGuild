package com.adventurersguild.equipment;

/**
 * Data-driven accessory definition (V0.6). Effects are applied server-side when
 * the item is equipped in a Curios slot (or held/offhand as a fallback).
 */
public class EquipmentData {
    private final String id;
    private final String itemId;
    private final String slot;
    /** Effect key: quest_exp / mining_speed / hostile_damage / move_speed / gold_reward. */
    private final String effect;
    /** Effect strength: e.g. 0.05 = +5%. */
    private final double value;
    private final String descriptionKey;

    public EquipmentData(String id, String itemId, String slot, String effect, double value, String descriptionKey) {
        this.id = id;
        this.itemId = itemId;
        this.slot = slot;
        this.effect = effect;
        this.value = value;
        this.descriptionKey = descriptionKey;
    }

    public String getId() { return id; }
    public String getItemId() { return itemId; }
    public String getSlot() { return slot; }
    public String getEffect() { return effect; }
    public double getValue() { return value; }
    public String getDescriptionKey() { return descriptionKey; }
}
