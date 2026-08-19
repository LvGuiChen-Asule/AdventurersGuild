package com.adventurersguild.client;

import com.adventurersguild.network.PartyDataSyncPacket;

/** Latest server-synced party data on the client. */
public final class ClientPartyData {
    private static PartyDataSyncPacket latest;

    private ClientPartyData() {}

    public static void update(PartyDataSyncPacket packet) {
        latest = packet;
    }

    public static PartyDataSyncPacket get() {
        return latest;
    }

    public static boolean hasParty() {
        return latest != null && !latest.getPartyId().isEmpty();
    }
}
