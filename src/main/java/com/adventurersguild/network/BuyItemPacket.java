package com.adventurersguild.network;

import com.adventurersguild.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server purchase request (V0.5 shop). */
public class BuyItemPacket {
    private final String shopId;
    private final int itemIndex;

    public BuyItemPacket(String shopId, int itemIndex) {
        this.shopId = shopId;
        this.itemIndex = itemIndex;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(shopId, 128);
        buf.writeVarInt(itemIndex);
    }

    public static BuyItemPacket decode(FriendlyByteBuf buf) {
        return new BuyItemPacket(buf.readUtf(128), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                QuestManager.buyItem(player, shopId, itemIndex);
            }
        });
        context.setPacketHandled(true);
    }
}
