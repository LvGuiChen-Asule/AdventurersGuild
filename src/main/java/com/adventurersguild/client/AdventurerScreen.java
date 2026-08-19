package com.adventurersguild.client;

import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.network.RegisterPlayerPacket;
import com.adventurersguild.player.AdventurerData;
import com.adventurersguild.player.LevelData;
import com.adventurersguild.player.ReputationData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Adventurer info (V0.2/V1.0): level title, EXP bar, reputation tier, gold, stats. */
public class AdventurerScreen extends AbstractGuildScreen {
    public AdventurerScreen(GuildDataSyncPacket data) {
        super(data, Component.translatable("ui.adventurersguild.adventurer.title"));
    }

    @Override
    protected void rebuildWidgets() {
        addNavBar();
        if (!data.isRegistered()) {
            int x = left();
            int y = top();
            addRenderableWidget(Button.builder(
                            Component.translatable("ui.adventurersguild.register"),
                            b -> GuildNetwork.sendToServer(new RegisterPlayerPacket()))
                    .bounds(x + (SCREEN_WIDTH - 120) / 2, y + 136, 120, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = left();
        int y = top();
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.adventurer.title"),
                x + 8, y + 8, TITLE_COLOR);

        if (!data.isRegistered()) {
            graphics.drawCenteredString(font,
                    Component.translatable("msg.adventurersguild.need_register"),
                    x + SCREEN_WIDTH / 2, y + 104, TEXT_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int lineY = y + 28;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.level").getString()
                        + ": Lv." + data.getLevel() + " "
                        + Component.translatable(LevelData.getTitleKey(data.getLevel())).getString(),
                x + 16, lineY, TITLE_COLOR);
        lineY += 15;

        String expText = data.getLevel() >= LevelData.MAX_LEVEL
                ? Component.translatable("ui.adventurersguild.max_level").getString()
                : data.getExperience() + " / " + LevelData.getExpForLevel(data.getLevel() + 1);
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.exp").getString() + ": " + expText,
                x + 16, lineY, TEXT_COLOR);
        int barX = x + 16;
        int barY = lineY + 10;
        int barWidth = SCREEN_WIDTH - 32;
        graphics.fill(barX, barY, barX + barWidth, barY + 6, 0xFF3A3A3A);
        graphics.fill(barX, barY, barX + (int) (barWidth * expRatio()), barY + 6, 0xFF4FC3F7);
        lineY += 26;

        String repText = Component.translatable("ui.adventurersguild.reputation").getString()
                + ": " + data.getReputation() + " ("
                + Component.translatable(ReputationData.getTierKey(data.getReputationTier())).getString() + ")";
        graphics.drawString(font, repText, x + 16, lineY, TEXT_COLOR);
        int repBarY = lineY + 10;
        graphics.fill(barX, repBarY, barX + barWidth, repBarY + 6, 0xFF3A3A3A);
        graphics.fill(barX, repBarY, barX + (int) (barWidth * repRatio()), repBarY + 6, 0xFFBA68C8);
        lineY += 24;

        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.gold").getString() + ": " + data.getGold(),
                x + 16, lineY, TITLE_COLOR);
        lineY += 18;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.completed_count").getString()
                        + ": " + data.getCompletedQuestCount(),
                x + 16, lineY, TEXT_COLOR);
        lineY += 18;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.active_count").getString()
                        + ": " + data.getActiveQuests().size() + "/" + AdventurerData.MAX_ACTIVE_QUESTS,
                x + 16, lineY, TEXT_COLOR);
        lineY += 18;
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.success_rate").getString()
                        + ": " + successRatePercent() + "%",
                x + 16, lineY, TEXT_COLOR);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private float expRatio() {
        if (data.getLevel() >= LevelData.MAX_LEVEL) {
            return 1f;
        }
        int intoCurrent = data.getExperience() - LevelData.getExpForLevel(data.getLevel());
        int span = LevelData.getExpForLevel(data.getLevel() + 1) - LevelData.getExpForLevel(data.getLevel());
        if (span <= 0) {
            return 0f;
        }
        return Math.min(1f, Math.max(0f, (float) intoCurrent / span));
    }

    private float repRatio() {
        int next = ReputationData.getNextThreshold(data.getReputation());
        if (next < 0) {
            return 1f;
        }
        int current = ReputationData.getThresholdForTier(data.getReputationTier());
        int span = next - current;
        if (span <= 0) {
            return 0f;
        }
        return Math.min(1f, Math.max(0f, (float) (data.getReputation() - current) / span));
    }

    private int successRatePercent() {
        int done = data.getCompletedQuestCount();
        int abandoned = data.getAbandonedQuestCount();
        int total = done + abandoned;
        return total <= 0 ? 0 : Math.round(100f * done / total);
    }
}
