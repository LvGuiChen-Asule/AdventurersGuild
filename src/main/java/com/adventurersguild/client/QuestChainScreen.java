package com.adventurersguild.client;

import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestChain;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Quest chains (V0.8): per-chain step status (locked / current / completed). */
public class QuestChainScreen extends AbstractGuildScreen {
    private static final int ROW_HEIGHT = 34;
    private static final int LIST_TOP = 32;
    private static final int LIST_BOTTOM = NAV_Y - 8;

    private static final class ChainRow {
        final QuestChain chain;
        final QuestChain.Step step;
        final int progress;
        ChainRow(QuestChain chain, QuestChain.Step step, int progress) {
            this.chain = chain;
            this.step = step;
            this.progress = progress;
        }
    }

    private final List<ChainRow> rows = new ArrayList<>();
    private int scrollOffset;
    private int maxVisibleRows = 1;

    public QuestChainScreen(GuildDataSyncPacket data) {
        super(data, Component.translatable("ui.adventurersguild.chains.title"));
    }

    @Override
    protected void rebuildWidgets() {
        rows.clear();
        for (int chainIndex = 0; chainIndex < data.getChains().size(); chainIndex++) {
            QuestChain chain = data.getChains().get(chainIndex);
            int progress = chainProgressFor(data, chainIndex);
            for (QuestChain.Step step : chain.getSteps()) {
                rows.add(new ChainRow(chain, step, progress));
            }
        }
        maxVisibleRows = Math.max(1, (LIST_BOTTOM - LIST_TOP) / ROW_HEIGHT);
        scrollOffset = Math.max(0, Math.min(Math.max(0, rows.size() - maxVisibleRows), scrollOffset));
        addNavBar();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (rows.size() > maxVisibleRows) {
            scrollOffset = Math.max(0, Math.min(rows.size() - maxVisibleRows, scrollOffset - (int) delta));
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
        graphics.drawString(font, Component.translatable("ui.adventurersguild.chains.title"),
                x + 8, y + 8, TITLE_COLOR);
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.chains.subtitle"),
                x + 8, y + 20, DIM_TEXT_COLOR);

        if (rows.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("ui.adventurersguild.chains.empty"),
                    x + SCREEN_WIDTH / 2, y + 114, DIM_TEXT_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int last = Math.min(rows.size(), scrollOffset + maxVisibleRows);
        for (int i = scrollOffset; i < last; i++) {
            ChainRow row = rows.get(i);
            int rowY = y + LIST_TOP + (i - scrollOffset) * ROW_HEIGHT;
            graphics.fill(x + 2, rowY, x + SCREEN_WIDTH - 2, rowY + ROW_HEIGHT - 2, 0x70000000);
            String questTitle = "?";
            Quest quest = findQuest(data, row.step.getQuestId());
            if (quest != null) {
                questTitle = font.plainSubstrByWidth(
                        Component.translatable(quest.getTitleKey()).getString(), SCREEN_WIDTH - 118);
            }
            int color;
            String statusKey;
            if (row.step.getStep() < row.progress) {
                color = ACCENT_COLOR;
                statusKey = "ui.adventurersguild.chain.done";
            } else if (row.step.getStep() == row.progress) {
                color = TITLE_COLOR;
                statusKey = "ui.adventurersguild.chain.current";
            } else {
                color = DIM_TEXT_COLOR;
                statusKey = "ui.adventurersguild.chain.locked";
            }
            graphics.drawString(font,
                    font.plainSubstrByWidth(
                            Component.translatable(row.chain.getTitleKey()).getString(), 46),
                    x + 8, rowY + 5, DIM_TEXT_COLOR);
            graphics.drawString(font,
                    Component.translatable("ui.adventurersguild.chain.step", row.step.getStep() + 1),
                    x + 56, rowY + 5, color);
            graphics.drawString(font, questTitle, x + 90, rowY + 5, TEXT_COLOR);
            graphics.drawString(font, Component.translatable(statusKey),
                    x + SCREEN_WIDTH - 64, rowY + 5, color);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
