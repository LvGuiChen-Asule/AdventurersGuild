package com.adventurersguild.client;

import com.adventurersguild.network.ChronicleUpdatePacket;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Latest server-synced chronicle data on the client (TASK-011). */
public final class ClientChronicleData {
    private static List<String> events = new ArrayList<>();
    private static List<String> lore = new ArrayList<>();
    private static Map<String, Integer> counters = new LinkedHashMap<>();

    private ClientChronicleData() {}

    public static void update(ChronicleUpdatePacket packet) {
        events = new ArrayList<>(packet.getEvents());
        lore = new ArrayList<>(packet.getLore());
        counters = new LinkedHashMap<>(packet.getCounters());
    }

    public static List<String> getEvents() { return events; }
    public static List<String> getLore() { return lore; }
    public static Map<String, Integer> getCounters() { return counters; }
}
