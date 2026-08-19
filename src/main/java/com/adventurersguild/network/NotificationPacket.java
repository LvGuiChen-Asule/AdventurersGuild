package com.adventurersguild.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client lightweight notification (chat-line display). */
public class NotificationPacket {
    private final String message;

    public NotificationPacket(String message) {
        this.message = message;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message, 512);
    }

    public static NotificationPacket decode(FriendlyByteBuf buf) {
        return new NotificationPacket(buf.readUtf(512));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> com.adventurersguild.client.ClientPacketHandlers.onNotification(this)));
        context.setPacketHandled(true);
    }

    public String getMessage() { return message; }
}
