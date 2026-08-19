package com.adventurersguild.client;

import com.adventurersguild.network.AcceptQuestPacket;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.network.RefreshQuestPacket;
import com.adventurersguild.network.RegisterPlayerPacket;
import com.adventurersguild.player.AdventurerData;
import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestChain;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Quest Hall (V1.0 structure): pinned tutorial quest, daily board quests
 * (quality-graded), unlocked chain quests, lock states and paid refresh.
 */
public class QuestBoardScreen extends AbstractGuildScreen {
    private static final int ROW_HEIGHT = 46;
    private static final int LIST_TOP = 32;
    private static final int LIST_BOTTOM = NAV_Y - 8;

    private final List<Quest> visibleQuests = new ArrayList<>();
    private int scrollOffset;
    private int maxVisibleRows = 1;

    public QuestBoardScreen(GuildDataSyncPacket data) {
        super(data, Component.translatable("ui.adventurersguild.hall.title"));
    }

    @Override
    protected void rebuildWidgets() {
        visibleQuests.clear();
        visibleQuests.addAll(buildVisibleQuests());
        maxVisibleRows = Math.max(1, (LIST_BOTTOM - LIST_TOP) / ROW_HEIGHT);
        scrollOffset = Math.max(0, Math.min(Math.max(0, visibleQuests.size() - maxVisibleRows), scrollOffset));
        addNavBar();

        int x = left();
        int y = top();
        if (!data.isRegistered()) {
            int buttonWidth = 120;
            addRenderableWidget(Button.builder(
                            Component.translatable("ui.adventurersguild.register"),
                            b -> GuildNetwork.sendToServer(new RegisterPlayerPacket()))
                    .bounds(x + (SCREEN_WIDTH - buttonWidth) / 2, y + 136, buttonWidth, 20).build());
            return;
        }

        String refreshLabel = Component.translatable("ui.adventurersguild.refresh").getString()
                + " (" + data.getNextRefreshCost() + "G)";
        addRenderableWidget(Button.builder(Component.literal(refreshLabel),
                        b -> GuildNetwork.sendToServer(new RefreshQuestPacket()))
                .bounds(x + SCREEN_WIDTH - 76, y + 6, 68, 16).build());

        int last = Math.min(visibleQuests.size(), scrollOffset + maxVisibleRows);
        for (int i = scrollOffset; i < last; i++) {
            Quest quest = visibleQuests.get(i);
            int rowY = y + LIST_TOP + (i - scrollOffset) * ROW_HEIGHT;
            addRenderableWidget(Button.builder(
                            Component.translatable("ui.adventurersguild.view"),
                            b -> minecraft.setScreen(new QuestDetailScreen(quest, data)))
                    .bounds(x + SCREEN_WIDTH - 132, rowY + 14, 56, 18).build());
            if (isLocked(quest)) {
                continue;
            }
            addRenderableWidget(Button.builder(
                            Component.translatable("ui.adventurersguild.accept"),
                            b -> GuildNetwork.sendToServer(new AcceptQuestPacket(quest.getId())))
                    .bounds(x + SCREEN_WIDTH - 70, rowY + 14, 62, 18).build());
        }
    }

    private List<Quest> buildVisibleQuests() {
        List<Quest> result = new ArrayList<>();
        List<String> addedIds = new ArrayList<>();
        Quest tutorial = null;
        for (String questId : data.getBoardQuestIds()) {
            Quest quest = findQuest(data, questId);
            if (quest == null) {
                continue;
            }
            if (quest.isTutorial()) {
                tutorial = quest;
            } else {
                result.add(quest);
                addedIds.add(quest.getId());
            }
        }
        if (tutorial != null && !data.isQuestCompleted(tutorial.getId())
                && !isActive(tutorial.getId())) {
            result.add(0, tutorial);
            addedIds.add(tutorial.getId());
        }
        for (int chainIndex = 0; chainIndex < data.getChains().size(); chainIndex++) {
            QuestChain chain = data.getChains().get(chainIndex);
            int progress = chainProgressFor(data, chainIndex);
            if (progress >= chain.getSteps().size()) {
                continue;
            }
            QuestChain.Step step = chain.getSteps().get(progress);
            Quest quest = findQuest(data, step.getQuestId());
            if (quest != null
                    && !addedIds.contains(quest.getId())
                    && !data.isQuestCompleted(quest.getId())
                    && !isActive(quest.getId())) {
                result.add(quest);
                addedIds.add(quest.getId());
            }
        }
        return result;
    }

    private boolean isActive(String questId) {
        return data.getActiveQuests().stream().anyMatch(v -> v.getQuestId().equals(questId));
    }

    private boolean isLocked(Quest quest) {
        return data.getLevel() < quest.getMinLevel() || data.getReputation() < quest.getMinReputation();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (data.isRegistered() && visibleQuests.size() > maxVisibleRows) {
            scrollOffset = Math.max(0, Math.min(
                    visibleQuests.size() - maxVisibleRows, scrollOffset - (int) delta));
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

        String title = font.plainSubstrByWidth(
                Component.translatable("ui.adventurersguild.hall.title").getString(), SCREEN_WIDTH - 90);
        graphics.drawString(font, title, x + 8, y + 8, TITLE_COLOR);
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.active_count").getString()
                        + ": " + data.getActiveQuests().size() + "/" + AdventurerData.MAX_ACTIVE_QUESTS
                        + "    "
                        + Component.translatable("ui.adventurersguild.reputation").getString()
                        + ": " + data.getReputation(),
                x + 8, y + 20, DIM_TEXT_COLOR);

        if (!data.isRegistered()) {
            graphics.drawCenteredString(font,
                    Component.translatable("ui.adventurersguild.not_registered"),
                    x + SCREEN_WIDTH / 2, y + 100, TEXT_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        if (visibleQuests.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("ui.adventurersguild.no_quests"),
                    x + SCREEN_WIDTH / 2, y + 114, DIM_TEXT_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int last = Math.min(visibleQuests.size(), scrollOffset + maxVisibleRows);
        for (int i = scrollOffset; i < last; i++) {
            Quest quest = visibleQuests.get(i);
            int rowY = y + LIST_TOP + (i - scrollOffset) * ROW_HEIGHT;
            graphics.fill(x + 2, rowY, x + SCREEN_WIDTH - 2, rowY + ROW_HEIGHT - 2, 0x80000000);
            String titleText = font.plainSubstrByWidth(
                    Component.translatable(quest.getTitleKey()).getString(), SCREEN_WIDTH - 84);
            graphics.drawString(font, titleText, x + 8, rowY + 4, qualityColor(quest.getQuality()));
            String typeText = typeLabel(quest.getType()).getString();
            graphics.drawString(font, typeText, x + SCREEN_WIDTH - 78, rowY + 4, typeColor(quest.getType()));
            graphics.drawString(font, objectiveLine(quest), x + 8, rowY + 17, TEXT_COLOR);
            if (isLocked(quest)) {
                Component lock = lockReason(quest);
                graphics.drawString(font, lock, x + 8, rowY + 29, LOCK_COLOR);
            } else {
                graphics.drawString(font, infoLine(quest), x + 8, rowY + 29, DIM_TEXT_COLOR);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Component lockReason(Quest quest) {
        if (data.getLevel() < quest.getMinLevel()) {
            return Component.translatable("ui.adventurersguild.lock_level", quest.getMinLevel());
        }
        return Component.translatable("ui.adventurersguild.lock_reputation", quest.getMinReputation());
    }
}
