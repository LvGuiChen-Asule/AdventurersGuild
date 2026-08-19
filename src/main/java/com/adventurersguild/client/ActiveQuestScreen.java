package com.adventurersguild.client;

import com.adventurersguild.network.AbandonQuestPacket;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.quest.Quest;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** My Quests: active quests of all types with progress bars, remaining time and abandon. */
public class ActiveQuestScreen extends AbstractGuildScreen {
    private static final int ROW_HEIGHT = 48;
    private static final int LIST_TOP = 32;
    private static final int LIST_BOTTOM = NAV_Y - 8;

    private final List<GuildDataSyncPacket.ActiveQuestView> views = new ArrayList<>();
    private int tickCounter;

    public ActiveQuestScreen(GuildDataSyncPacket data) {
        super(data, Component.translatable("ui.adventurersguild.myquests.title"));
    }

    @Override
    protected void rebuildWidgets() {
        views.clear();
        views.addAll(data.getActiveQuests());
        addNavBar();
        if (views.isEmpty()) {
            return;
        }
        int x = left();
        int y = top();
        int last = Math.min(views.size(), (LIST_BOTTOM - LIST_TOP) / ROW_HEIGHT);
        for (int i = 0; i < last; i++) {
            GuildDataSyncPacket.ActiveQuestView view = views.get(i);
            int rowY = y + LIST_TOP + i * ROW_HEIGHT;
            addRenderableWidget(Button.builder(
                            Component.translatable("ui.adventurersguild.abandon"),
                            b -> GuildNetwork.sendToServer(new AbandonQuestPacket(view.getQuestId())))
                    .bounds(x + SCREEN_WIDTH - 64, rowY + 14, 56, 20).build());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (++tickCounter % 20 == 0) {
            for (GuildDataSyncPacket.ActiveQuestView view : views) {
                view.tickDownRemaining();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = left();
        int y = top();

        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.myquests.title"),
                x + 8, y + 8, TITLE_COLOR);
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.completed_count").getString()
                        + ": " + data.getCompletedQuestCount(),
                x + 8, y + 20, DIM_TEXT_COLOR);

        if (views.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("ui.adventurersguild.no_active"),
                    x + SCREEN_WIDTH / 2, y + 114, DIM_TEXT_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int last = Math.min(views.size(), (LIST_BOTTOM - LIST_TOP) / ROW_HEIGHT);
        for (int i = 0; i < last; i++) {
            GuildDataSyncPacket.ActiveQuestView view = views.get(i);
            Quest quest = findQuest(data, view.getQuestId());
            int rowY = y + LIST_TOP + i * ROW_HEIGHT;
            graphics.fill(x + 2, rowY, x + SCREEN_WIDTH - 2, rowY + ROW_HEIGHT - 2, 0x80000000);

            String title = font.plainSubstrByWidth(
                    Component.translatable(view.getTitleKey()).getString(), SCREEN_WIDTH - 104);
            graphics.drawString(font, title, x + 8, rowY + 4, quest != null ? qualityColor(quest.getQuality()) : TITLE_COLOR);
            graphics.drawString(font,
                    Component.translatable("ui.adventurersguild.status." + view.getStatus()),
                    x + SCREEN_WIDTH - 76, rowY + 4, DIM_TEXT_COLOR);
            graphics.drawString(font,
                    Component.translatable("ui.adventurersguild.progress_line", view.getProgress(), view.getTarget()),
                    x + 8, rowY + 17, TEXT_COLOR);
            if (view.getRemainingSeconds() >= 0) {
                graphics.drawString(font,
                        Component.translatable("ui.adventurersguild.remaining_line",
                                formatTime((int) view.getRemainingSeconds())),
                        x + 76, rowY + 17, DIM_TEXT_COLOR);
            }
            if (quest != null) {
                String objective = font.plainSubstrByWidth(objectiveLine(quest).getString(), SCREEN_WIDTH - 100);
                graphics.drawString(font, objective, x + 8, rowY + 29, DIM_TEXT_COLOR);
            }

            int barX = x + 8;
            int barY = rowY + 41;
            int barWidth = SCREEN_WIDTH - 84;
            float ratio = view.getTarget() > 0
                    ? Math.min(1f, (float) view.getProgress() / view.getTarget())
                    : 0f;
            graphics.fill(barX, barY, barX + barWidth, barY + 4, 0xFF3A3A3A);
            graphics.fill(barX, barY, barX + (int) (barWidth * ratio), barY + 4, ACCENT_COLOR);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
