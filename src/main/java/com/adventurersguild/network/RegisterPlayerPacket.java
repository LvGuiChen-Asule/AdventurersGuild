package com.adventurersguild.network;

import com.adventurersguild.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server request to register as an adventurer. */
public class RegisterPlayerPacket {
    public RegisterPlayerPacket() {}

    public void encode(FriendlyByteBuf buf) {}

    public static RegisterPlayerPacket decode(FriendlyByteBuf buf) {
        return new RegisterPlayerPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                QuestManager.register(player);
            }
        });
        context.setPacketHandled(true);
    }
}
