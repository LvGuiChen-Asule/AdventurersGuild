package com.adventurersguild.network;

import com.adventurersguild.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server request to accept a quest. The server validates everything. */
public class AcceptQuestPacket {
    private final String questId;

    public AcceptQuestPacket(String questId) {
        this.questId = questId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId, 128);
    }

    public static AcceptQuestPacket decode(FriendlyByteBuf buf) {
        return new AcceptQuestPacket(buf.readUtf(128));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                QuestManager.acceptQuest(player, questId);
            }
        });
        context.setPacketHandled(true);
    }
}
