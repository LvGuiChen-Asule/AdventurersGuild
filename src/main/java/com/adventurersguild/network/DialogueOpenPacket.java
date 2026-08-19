package com.adventurersguild.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server -> client: open/refresh a dialogue node (TASK-010). */
public class DialogueOpenPacket {
    private final String dialogueId;
    private final String npcRole;
    private final String nodeId;
    private final String textKey;
    private final List<String> choiceKeys;

    public DialogueOpenPacket(String dialogueId, String npcRole, String nodeId,
                              String textKey, List<String> choiceKeys) {
        this.dialogueId = dialogueId;
        this.npcRole = npcRole;
        this.nodeId = nodeId;
        this.textKey = textKey;
        this.choiceKeys = choiceKeys;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(dialogueId);
        buf.writeUtf(npcRole);
        buf.writeUtf(nodeId);
        buf.writeUtf(textKey);
        buf.writeCollection(choiceKeys, FriendlyByteBuf::writeUtf);
    }

    public static DialogueOpenPacket decode(FriendlyByteBuf buf) {
        String dialogueId = buf.readUtf(128);
        String npcRole = buf.readUtf(64);
        String nodeId = buf.readUtf(128);
        String textKey = buf.readUtf(256);
        List<String> choiceKeys = new ArrayList<>(buf.readList(b -> b.readUtf(256)));
        return new DialogueOpenPacket(dialogueId, npcRole, nodeId, textKey, choiceKeys);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> com.adventurersguild.client.ClientPacketHandlers.onDialogueOpen(this)));
        context.setPacketHandled(true);
    }

    public String getDialogueId() { return dialogueId; }
    public String getNpcRole() { return npcRole; }
    public String getNodeId() { return nodeId; }
    public String getTextKey() { return textKey; }
    public List<String> getChoiceKeys() { return choiceKeys; }
}
