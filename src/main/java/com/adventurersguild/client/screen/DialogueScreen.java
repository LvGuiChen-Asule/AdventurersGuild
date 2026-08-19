package com.adventurersguild.client.screen;

import com.adventurersguild.client.AbstractGuildScreen;
import com.adventurersguild.network.DialogueChoicePacket;
import com.adventurersguild.network.DialogueOpenPacket;
import com.adventurersguild.network.GuildNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** TASK-010: dialogue screen - NPC name, text and available choices. */
public class DialogueScreen extends AbstractGuildScreen {
    private static final int TEXT_TOP = 38;
    private static final int CHOICE_TOP = 96;
    private static final int CHOICE_HEIGHT = 20;

    private DialogueOpenPacket packet;

    public DialogueScreen(DialogueOpenPacket packet) {
        super(Component.translatable("ui.adventurersguild.dialogue.title"));
        this.packet = packet;
    }

    @Override
    protected void rebuildWidgets() {
        int x = left();
        int y = top();
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.dialogue.close"), b -> onClose())
                .bounds(x + SCREEN_WIDTH - 60, y + 8, 52, 16).build());
        List<String> choices = packet.getChoiceKeys();
        int startY = top() + CHOICE_TOP;
        for (int i = 0; i < choices.size(); i++) {
            final int index = i;
            addRenderableWidget(Button.builder(
                            Component.translatable(choices.get(i)),
                            b -> GuildNetwork.sendToServer(new DialogueChoicePacket(
                                    packet.getDialogueId(), packet.getNodeId(), index)))
                    .bounds(x + 16, startY + i * (CHOICE_HEIGHT + 4), SCREEN_WIDTH - 32, CHOICE_HEIGHT)
                    .build());
        }
    }

    /** Called when the server sends the next node while this screen is open. */
    public void refreshNode(DialogueOpenPacket newPacket) {
        this.packet = newPacket;
        init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = left();
        int y = top();
        graphics.drawString(font,
                Component.translatable("npc.adventurersguild." + packet.getNpcRole()),
                x + 10, y + 10, TITLE_COLOR);
        graphics.drawWordWrap(font,
                Component.translatable(packet.getTextKey()),
                x + 12, y + TEXT_TOP, SCREEN_WIDTH - 24, TEXT_COLOR);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
