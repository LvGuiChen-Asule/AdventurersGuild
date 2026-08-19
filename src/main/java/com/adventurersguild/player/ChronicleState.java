package com.adventurersguild.player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * TASK-002: chronicle state (TASK-011 backing store) - world events the player
 * has triggered, lore discovered, and per-player stat counters.
 * All persisted inside {@link AdventurerData} NBT.
 */
public class ChronicleState {
    private final AdventurerData data;

    public ChronicleState(AdventurerData data) {
        this.data = data;
    }

    public boolean hasEvent(String eventId) {
        return data.getChronicleEvents().contains(eventId);
    }

    public void recordEvent(String eventId) {
        data.getChronicleEvents().add(eventId);
    }

    public Set<String> getEvents() {
        return Collections.unmodifiableSet(data.getChronicleEvents());
    }

    public boolean hasLore(String loreId) {
        return data.getLoreDiscovered().contains(loreId);
    }

    public void discoverLore(String loreId) {
        data.getLoreDiscovered().add(loreId);
    }

    public Set<String> getLore() {
        return Collections.unmodifiableSet(data.getLoreDiscovered());
    }

    public int getCounter(String key) {
        return data.getCounters().getOrDefault(key, 0);
    }

    public void incrementCounter(String key, int amount) {
        data.getCounters().merge(key, Math.max(0, amount), Integer::sum);
    }

    public Map<String, Integer> getCounters() {
        return Collections.unmodifiableMap(data.getCounters());
    }
}
