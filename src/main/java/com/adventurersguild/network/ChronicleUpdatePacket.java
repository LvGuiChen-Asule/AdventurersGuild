package com.adventurersguild.network;

import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.ChronicleState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Server -> client: chronicle sync (events / lore / counters) (TASK-011). */
public class ChronicleUpdatePacket {
    private final List<String> events;
    private final List<String> lore;
    private final Map<String, Integer> counters;
    private final boolean openScreen;

    public ChronicleUpdatePacket(List<String> events, List<String> lore,
                                 Map<String, Integer> counters, boolean openScreen) {
        this.events = events;
        this.lore = lore;
        this.counters = counters;
        this.openScreen = openScreen;
    }

    public static ChronicleUpdatePacket forPlayer(ServerPlayer player, boolean openScreen) {
        ChronicleState state = AdventurerCapability.getChronicleState(player);
        return new ChronicleUpdatePacket(
                state != null ? new ArrayList<>(state.getEvents()) : new ArrayList<>(),
                state != null ? new ArrayList<>(state.getLore()) : new ArrayList<>(),
                state != null ? new LinkedHashMap<>(state.getCounters()) : new LinkedHashMap<>(),
                openScreen);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(events, FriendlyByteBuf::writeUtf);
        buf.writeCollection(lore, FriendlyByteBuf::writeUtf);
        buf.writeVarInt(counters.size());
        for (Map.Entry<String, Integer> entry : counters.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
        buf.writeBoolean(openScreen);
    }

    public static ChronicleUpdatePacket decode(FriendlyByteBuf buf) {
        List<String> events = new ArrayList<>(buf.readList(b -> b.readUtf(128)));
        List<String> lore = new ArrayList<>(buf.readList(b -> b.readUtf(128)));
        int counterCount = buf.readVarInt();
        Map<String, Integer> counters = new LinkedHashMap<>();
        for (int i = 0; i < counterCount; i++) {
            counters.put(buf.readUtf(128), buf.readVarInt());
        }
        boolean openScreen = buf.readBoolean();
        return new ChronicleUpdatePacket(events, lore, counters, openScreen);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> com.adventurersguild.client.ClientPacketHandlers.onChronicleUpdate(this)));
        context.setPacketHandled(true);
    }

    public List<String> getEvents() { return events; }
    public List<String> getLore() { return lore; }
    public Map<String, Integer> getCounters() { return counters; }
    public boolean shouldOpenScreen() { return openScreen; }
}
