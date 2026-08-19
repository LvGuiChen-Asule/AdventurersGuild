package com.adventurersguild.client;

import com.adventurersguild.network.GuildDataSyncPacket;

/** Latest server-synced guild data kept on the client for screen rendering. */
public final class ClientGuildData {
    private static GuildDataSyncPacket latest;

    private ClientGuildData() {}

    public static void update(GuildDataSyncPacket packet) {
        latest = packet;
    }

    public static GuildDataSyncPacket get() {
        return latest;
    }

    public static boolean has() {
        return latest != null;
    }
}
