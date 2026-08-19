package com.adventurersguild.network;

import com.adventurersguild.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server request to abandon an active quest. */
public class AbandonQuestPacket {
    private final String questId;

    public AbandonQuestPacket(String questId) {
        this.questId = questId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId, 128);
    }

    public static AbandonQuestPacket decode(FriendlyByteBuf buf) {
        return new AbandonQuestPacket(buf.readUtf(128));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                QuestManager.abandonQuest(player, questId);
            }
        });
        context.setPacketHandled(true);
    }
}
