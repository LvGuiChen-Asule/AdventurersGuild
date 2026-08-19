package com.adventurersguild.network;

import com.adventurersguild.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server request to refresh the daily quest board (V0.4). */
public class RefreshQuestPacket {
    public RefreshQuestPacket() {}

    public void encode(FriendlyByteBuf buf) {}

    public static RefreshQuestPacket decode(FriendlyByteBuf buf) {
        return new RefreshQuestPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                QuestManager.refreshBoard(player);
            }
        });
        context.setPacketHandled(true);
    }
}
