package com.adventurersguild.client.screen;

import com.adventurersguild.client.AbstractGuildScreen;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.network.OpenGuildScreenPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * TASK-020: Guild Overview - the landing screen (keybind G).
 * Shows the current chapter, adventurer summary, chapters and party status.
 */
public class GuildMainScreen extends AbstractGuildScreen {
    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 92;
    private static final int LIST_BOTTOM = NAV_Y - 8;

    public GuildMainScreen(GuildDataSyncPacket data) {
        super(data, Component.translatable("ui.adventurersguild.main.title"));
    }

    @Override
    protected void rebuildWidgets() {
        addNavBar();
        int x = left();
        int y = top();
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                        Component.translatable("ui.adventurersguild.chronicle.title"),
                        b -> GuildNetwork.sendToServer(new OpenGuildScreenPacket("chronicle")))
                .bounds(x + 8, y + 62, 84, 18).build());
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                        Component.translatable("ui.adventurersguild.party.title"),
                        b -> minecraft.setScreen(new PartyScreen(data)))
                .bounds(x + 100, y + 62, 84, 18).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = left();
        int y = top();
        graphics.drawCenteredString(font,
                Component.translatable("ui.adventurersguild.main.title"),
                x + SCREEN_WIDTH / 2, y + 8, TITLE_COLOR);

        String chapterId = currentChapterId();
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.main.chapter",
                        Component.translatable("chapter.adventurersguild." + chapterId + ".title")),
                x + 10, y + 32, TITLE_COLOR);
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.main.level",
                        data.getLevel(), data.getReputation(), data.getGold()),
                x + 10, y + 46, TEXT_COLOR);

        int rowY = y + LIST_TOP;
        for (int i = 0; i < data.getChapterIds().size(); i++) {
            String id = data.getChapterIds().get(i);
            boolean unlocked = i < data.getChapterUnlocked().size() && data.getChapterUnlocked().get(i);
            graphics.fill(x + 2, rowY, x + SCREEN_WIDTH - 2, rowY + ROW_HEIGHT - 2, 0x60000000);
            String title = font.plainSubstrByWidth(
                    Component.translatable("chapter.adventurersguild." + id + ".title").getString(),
                    SCREEN_WIDTH - 60);
            graphics.drawString(font, title, x + 8, rowY + 6,
                    unlocked ? TITLE_COLOR : DIM_TEXT_COLOR);
            graphics.drawString(font,
                    Component.translatable(unlocked
                            ? "ui.adventurersguild.main.unlocked"
                            : "ui.adventurersguild.main.locked"),
                    x + SCREEN_WIDTH - 52, rowY + 6,
                    unlocked ? ACCENT_COLOR : LOCK_COLOR);
            rowY += ROW_HEIGHT;
        }

        String partyLine = data.getPartyName().isEmpty()
                ? Component.translatable("ui.adventurersguild.party.none").getString()
                : Component.translatable("ui.adventurersguild.party.summary",
                        data.getPartyName(), data.getPartyMemberCount(), data.getPartyLevel()).getString();
        graphics.drawString(font, partyLine, x + 10, y + LIST_BOTTOM - 4, DIM_TEXT_COLOR);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private String currentChapterId() {
        String current = "chapter_0";
        for (int i = 0; i < data.getChapterIds().size(); i++) {
            if (i < data.getChapterUnlocked().size() && data.getChapterUnlocked().get(i)) {
                current = data.getChapterIds().get(i);
            }
        }
        return current;
    }
}
