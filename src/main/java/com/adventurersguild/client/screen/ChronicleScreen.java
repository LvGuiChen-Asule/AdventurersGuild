package com.adventurersguild.client.screen;

import com.adventurersguild.client.AbstractGuildScreen;
import com.adventurersguild.network.ChronicleUpdatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TASK-011 base chronicle screen: world events + discovered lore + counters.
 * Full restyle happens in TASK-020 (deep wood / parchment theme).
 */
public class ChronicleScreen extends AbstractGuildScreen {
    private static final int ROW_HEIGHT = 18;
    private static final int LIST_TOP = 34;
    private static final int LIST_BOTTOM = NAV_Y - 8;

    private final List<String> lines = new ArrayList<>();
    private int scrollOffset;
    private int maxVisibleRows = 1;

    public ChronicleScreen(ChronicleUpdatePacket packet) {
        super(Component.translatable("ui.adventurersguild.chronicle.title"));
        for (String eventId : packet.getEvents()) {
            lines.add(Component.translatable("ui.adventurersguild.chronicle.event")
                    .getString() + ": " + Component.translatable("event.adventurersguild." + eventId).getString());
        }
        for (String loreId : packet.getLore()) {
            lines.add(Component.translatable("ui.adventurersguild.chronicle.lore")
                    .getString() + ": " + Component.translatable("lore.adventurersguild." + loreId).getString());
        }
        for (Map.Entry<String, Integer> entry : packet.getCounters().entrySet()) {
            if (entry.getKey().startsWith("visit.")) {
                String role = entry.getKey().substring("visit.".length());
                lines.add(Component.translatable("ui.adventurersguild.chronicle.visits",
                        Component.translatable("npc.adventurersguild." + role), entry.getValue()).getString());
            }
        }
        if (lines.isEmpty()) {
            lines.add(Component.translatable("ui.adventurersguild.chronicle.empty").getString());
        }
    }

    @Override
    protected void rebuildWidgets() {
        addNavBar();
        maxVisibleRows = Math.max(1, (LIST_BOTTOM - LIST_TOP) / ROW_HEIGHT);
        scrollOffset = Math.max(0, Math.min(Math.max(0, lines.size() - maxVisibleRows), scrollOffset));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (lines.size() > maxVisibleRows) {
            scrollOffset = Math.max(0, Math.min(lines.size() - maxVisibleRows, scrollOffset - (int) delta));
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = left();
        int y = top();
        graphics.drawString(font, Component.translatable("ui.adventurersguild.chronicle.title"),
                x + 8, y + 8, TITLE_COLOR);
        graphics.drawString(font, Component.translatable("ui.adventurersguild.chronicle.subtitle"),
                x + 8, y + 20, DIM_TEXT_COLOR);

        int last = Math.min(lines.size(), scrollOffset + maxVisibleRows);
        for (int i = scrollOffset; i < last; i++) {
            int rowY = y + LIST_TOP + (i - scrollOffset) * ROW_HEIGHT;
            graphics.fill(x + 2, rowY, x + SCREEN_WIDTH - 2, rowY + ROW_HEIGHT - 2, 0x60000000);
            String line = font.plainSubstrByWidth(lines.get(i), SCREEN_WIDTH - 20);
            graphics.drawString(font, line, x + 8, rowY + 5, TEXT_COLOR);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
