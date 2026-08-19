package com.adventurersguild.client;

import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.quest.Quest;
import com.adventurersguild.quest.QuestQuality;
import com.adventurersguild.quest.QuestType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Shared layout, colors and helpers for all guild screens. */
public abstract class AbstractGuildScreen extends Screen {
    protected static final int SCREEN_WIDTH = 264;
    protected static final int SCREEN_HEIGHT = 234;
    protected static final int NAV_Y = SCREEN_HEIGHT - 22;

    protected static final int TITLE_COLOR = 0xFFE8D5A3;   // parchment gold
    protected static final int TEXT_COLOR = 0xFFEAE2D2;    // parchment
    protected static final int DIM_TEXT_COLOR = 0xFF9C8F7A;
    protected static final int ACCENT_COLOR = 0xFF3E6B9E;  // deep blue
    protected static final int HUNT_COLOR = 0xFFE06C5B;
    protected static final int PANEL_COLOR = 0xE625170E;   // deep wood
    protected static final int BORDER_COLOR = 0xFF8A6D3B;  // dark gold
    protected static final int HEADER_BAND_COLOR = 0x8A2B2117;
    protected static final int LOCK_COLOR = 0xFFE06C5B;
    protected static final int RARE_COLOR = 0xFF4FC3F7;
    protected static final int EPIC_COLOR = 0xFFBA68C8;
    protected static final int LEGENDARY_COLOR = 0xFFFF8F00;

    protected GuildDataSyncPacket data;

    protected AbstractGuildScreen(GuildDataSyncPacket data, Component title) {
        super(title);
        this.data = data;
    }

    /** Constructor for screens without a guild data snapshot (e.g. dialogue). */
    protected AbstractGuildScreen(Component title) {
        this(null, title);
    }

    @Override
    public void init() {
        clearWidgets();
        rebuildWidgets();
    }

    protected abstract void rebuildWidgets();

    /** Called when a fresh server snapshot arrives while this screen is open. */
    public void refresh(GuildDataSyncPacket newData) {
        this.data = newData;
        init();
    }

    protected int left() {
        return (this.width - SCREEN_WIDTH) / 2;
    }

    protected int top() {
        return (this.height - SCREEN_HEIGHT) / 2;
    }

    protected void addNavBar() {
        int x = left();
        int buttonWidth = (SCREEN_WIDTH - 5 * 4) / 5;
        int y = top() + NAV_Y;
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.nav.hall"), b -> openHall())
                .bounds(x, y, buttonWidth, 18).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.nav.myquests"), b -> openMyQuests())
                .bounds(x + buttonWidth + 4, y, buttonWidth, 18).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.nav.adventurer"), b -> openAdventurer())
                .bounds(x + 2 * (buttonWidth + 4), y, buttonWidth, 18).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.nav.shop"), b -> openShop())
                .bounds(x + 3 * (buttonWidth + 4), y, buttonWidth, 18).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("ui.adventurersguild.nav.chains"), b -> openChains())
                .bounds(x + 4 * (buttonWidth + 4), y, buttonWidth, 18).build());
    }

    protected void openHall() {
        if (ClientGuildData.has()) {
            minecraft.setScreen(new QuestBoardScreen(ClientGuildData.get()));
        }
    }

    protected void openMyQuests() {
        if (ClientGuildData.has()) {
            minecraft.setScreen(new ActiveQuestScreen(ClientGuildData.get()));
        }
    }

    protected void openAdventurer() {
        if (ClientGuildData.has()) {
            minecraft.setScreen(new AdventurerScreen(ClientGuildData.get()));
        }
    }

    protected void openShop() {
        if (ClientGuildData.has()) {
            minecraft.setScreen(new ShopScreen(ClientGuildData.get()));
        }
    }

    protected void openChains() {
        if (ClientGuildData.has()) {
            minecraft.setScreen(new QuestChainScreen(ClientGuildData.get()));
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        int x = left();
        int y = top();
        graphics.fill(x, y, x + SCREEN_WIDTH, y + SCREEN_HEIGHT, PANEL_COLOR);
        graphics.renderOutline(x, y, x + SCREEN_WIDTH, y + SCREEN_HEIGHT, BORDER_COLOR);
        graphics.fill(x + 1, y + 1, x + SCREEN_WIDTH - 1, y + 26, HEADER_BAND_COLOR);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------- shared formatting helpers ----------

    protected static Component typeLabel(QuestType type) {
        return Component.translatable("quest.type." + type.name());
    }

    protected static int typeColor(QuestType type) {
        return type == QuestType.HUNT ? HUNT_COLOR : ACCENT_COLOR;
    }

    protected static int qualityColor(QuestQuality quality) {
        return switch (quality) {
            case COMMON -> TEXT_COLOR;
            case UNCOMMON -> ACCENT_COLOR;
            case RARE -> RARE_COLOR;
            case EPIC -> EPIC_COLOR;
            case LEGENDARY -> LEGENDARY_COLOR;
        };
    }

    protected static Component formatTime(int seconds) {
        if (seconds <= 0) {
            return Component.literal("∞");
        }
        int totalMinutes = Math.max(1, (int) Math.ceil(seconds / 60.0));
        if (totalMinutes < 60) {
            return Component.translatable("ui.adventurersguild.minutes", totalMinutes);
        }
        return Component.translatable("ui.adventurersguild.hours_minutes", totalMinutes / 60, totalMinutes % 60);
    }

    protected static String targetKey(Quest quest) {
        return quest.getTarget();
    }

    protected static boolean isValidTarget(Quest quest) {
        return quest.getTarget() != null && !quest.getTarget().isEmpty();
    }

    /** A displayable name for the quest target (item/entity name, or raw tag). */
    protected static Component targetName(Quest quest) {
        if (!isValidTarget(quest)) {
            return Component.literal("?");
        }
        if (quest.getTarget().startsWith("#")) {
            return Component.literal(quest.getTarget());
        }
        var location = net.minecraft.resources.ResourceLocation.tryParse(quest.getTarget());
        if (location == null) {
            return Component.literal(quest.getTarget());
        }
        if (quest.getType() == QuestType.HUNT) {
            return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                    .getOptional(location)
                    .map(net.minecraft.world.entity.EntityType::getDescription)
                    .orElse(Component.literal(quest.getTarget()));
        }
        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(location);
        return item != null && item != Items.AIR
                ? item.getDescription()
                : Component.literal(quest.getTarget());
    }

    protected static Component objectiveLine(Quest quest) {
        return switch (quest.getType()) {
            case COLLECT -> Component.translatable("ui.adventurersguild.objective.collect",
                    targetName(quest), quest.getAmount());
            case HUNT, ELITE -> Component.translatable("ui.adventurersguild.objective.hunt",
                    targetName(quest), quest.getAmount());
            case EXPLORE -> Component.translatable("ui.adventurersguild.objective.explore", targetName(quest));
            case SURVIVE -> Component.translatable("ui.adventurersguild.objective.survive",
                    formatTime(quest.getAmount()));
            case TRANSPORT -> Component.translatable("ui.adventurersguild.objective.transport",
                    targetName(quest), quest.getAmount(), quest.getExtra().isBlank() ? "?" : quest.getExtra());
            case INTERACT -> Component.translatable("ui.adventurersguild.objective.interact",
                    Component.translatable("npc.adventurersguild." + quest.getTarget()));
            case MILESTONE -> Component.translatable("ui.adventurersguild.objective.milestone",
                    Component.translatable("event.adventurersguild." + quest.getTarget()));
            case ACHIEVEMENT -> Component.translatable("ui.adventurersguild.objective.achievement",
                    Component.literal(quest.getTarget()), quest.getAmount());
        };
    }

    protected static Component infoLine(Quest quest) {
        return Component.translatable("ui.adventurersguild.reward_line",
                        quest.getGoldReward(), quest.getExpReward())
                .copy()
                .append("    ")
                .append(Component.translatable("ui.adventurersguild.level_line", quest.getRecommendedLevel()))
                .append("    ")
                .append(Component.translatable("ui.adventurersguild.time_line", formatTime(quest.getTimeLimitSeconds())));
    }

    protected static Quest findQuest(GuildDataSyncPacket data, String questId) {
        for (Quest quest : data.getQuests()) {
            if (quest.getId().equals(questId)) {
                return quest;
            }
        }
        return null;
    }

    protected static int chainProgressFor(GuildDataSyncPacket data, int chainIndex) {
        if (chainIndex < 0 || chainIndex >= data.getChainProgress().size()) {
            return 0;
        }
        return data.getChainProgress().get(chainIndex);
    }
}
