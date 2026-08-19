package com.adventurersguild.client;

import com.adventurersguild.network.AcceptQuestPacket;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Full quest detail: objective, rewards, level, time limit and accept action. */
public class QuestDetailScreen extends AbstractGuildScreen {
    private final Quest quest;

    public QuestDetailScreen(Quest quest, GuildDataSyncPacket data) {
        super(data, Component.translatable(quest.getTitleKey()));
        this.quest = quest;
    }

    @Override
    protected void rebuildWidgets() {
        addNavBar();
        int x = left();
        int y = top();
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.back"), b -> openHall())
                .bounds(x + 8, y + NAV_Y - 28, 72, 20).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.accept"),
                        b -> GuildNetwork.sendToServer(new AcceptQuestPacket(quest.getId())))
                .bounds(x + SCREEN_WIDTH - 80, y + NAV_Y - 28, 72, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = left();
        int y = top();

        String title = font.plainSubstrByWidth(
                Component.translatable(quest.getTitleKey()).getString(), SCREEN_WIDTH - 12);
        graphics.drawCenteredString(font, title, x + SCREEN_WIDTH / 2, y + 8, TITLE_COLOR);
        graphics.drawCenteredString(font, typeLabel(quest.getType()), x + SCREEN_WIDTH / 2, y + 20, typeColor(quest.getType()));

        int lineY = y + 38;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.objective").copy().append(": ").append(objectiveLine()),
                x + 10, lineY, TEXT_COLOR);
        lineY += 12;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.reward").copy().append(": ")
                        .append(Component.translatable("ui.adventurersguild.reward_line",
                                quest.getGoldReward(), quest.getExpReward())),
                x + 10, lineY, TITLE_COLOR);
        lineY += 12;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.recommended_level").copy().append(": ")
                        .append(Component.translatable("ui.adventurersguild.level_line", quest.getRecommendedLevel())),
                x + 10, lineY, TEXT_COLOR);
        lineY += 12;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.time_limit").copy().append(": ")
                        .append(formatTime(quest.getTimeLimitSeconds())),
                x + 10, lineY, TEXT_COLOR);
        lineY += 16;

        graphics.drawWordWrap(font,
                Component.translatable(quest.getDescriptionKey()),
                x + 10, lineY, SCREEN_WIDTH - 20, DIM_TEXT_COLOR);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Component objectiveLine() {
        return Component.translatable(
                quest.getType() == QuestType.COLLECT
                        ? "ui.adventurersguild.objective.collect"
                        : "ui.adventurersguild.objective.hunt",
                targetName(quest), quest.getAmount());
    }
}
