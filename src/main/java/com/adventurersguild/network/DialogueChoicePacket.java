package com.adventurersguild.network;

import com.adventurersguild.dialogue.DialogueManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: select a dialogue choice (TASK-010). */
public class DialogueChoicePacket {
    private final String dialogueId;
    private final String nodeId;
    private final int choiceIndex;

    public DialogueChoicePacket(String dialogueId, String nodeId, int choiceIndex) {
        this.dialogueId = dialogueId;
        this.nodeId = nodeId;
        this.choiceIndex = choiceIndex;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(dialogueId);
        buf.writeUtf(nodeId);
        buf.writeVarInt(choiceIndex);
    }

    public static DialogueChoicePacket decode(FriendlyByteBuf buf) {
        return new DialogueChoicePacket(buf.readUtf(128), buf.readUtf(128), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                DialogueManager.onChoice(player, dialogueId, nodeId, choiceIndex);
            }
        });
        context.setPacketHandled(true);
    }
}
