package com.adventurersguild.client;

import com.adventurersguild.economy.Shop;
import com.adventurersguild.economy.ShopItem;
import com.adventurersguild.network.BuyItemPacket;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Guild shop (V0.5): all shop categories with level unlocks and purchase buttons. */
public class ShopScreen extends AbstractGuildScreen {
    private static final int ROW_HEIGHT = 28;
    private static final int LIST_TOP = 34;
    private static final int LIST_BOTTOM = NAV_Y - 8;

    private static final class Row {
        final Shop shop;
        final ShopItem item;
        final int index;
        Row(Shop shop, ShopItem item, int index) {
            this.shop = shop;
            this.item = item;
            this.index = index;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private int scrollOffset;
    private int maxVisibleRows = 1;

    public ShopScreen(GuildDataSyncPacket data) {
        super(data, Component.translatable("ui.adventurersguild.shop.title"));
    }

    @Override
    protected void rebuildWidgets() {
        rows.clear();
        for (Shop shop : data.getShops()) {
            for (int i = 0; i < shop.getItems().size(); i++) {
                rows.add(new Row(shop, shop.getItems().get(i), i));
            }
        }
        maxVisibleRows = Math.max(1, (LIST_BOTTOM - LIST_TOP) / ROW_HEIGHT);
        scrollOffset = Math.max(0, Math.min(Math.max(0, rows.size() - maxVisibleRows), scrollOffset));
        addNavBar();

        int x = left();
        int y = top();
        int last = Math.min(rows.size(), scrollOffset + maxVisibleRows);
        for (int i = scrollOffset; i < last; i++) {
            Row row = rows.get(i);
            int rowY = y + LIST_TOP + (i - scrollOffset) * ROW_HEIGHT;
            boolean locked = data.getLevel() < row.item.getMinLevel();
            if (locked) {
                continue;
            }
            addRenderableWidget(Button.builder(
                            Component.translatable("ui.adventurersguild.buy"),
                            b -> GuildNetwork.sendToServer(new BuyItemPacket(row.shop.getId(), row.index)))
                    .bounds(x + SCREEN_WIDTH - 62, rowY + 5, 54, 18).build());
        }
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
        graphics.drawString(font, Component.translatable("ui.adventurersguild.shop.title"),
                x + 8, y + 8, TITLE_COLOR);
        graphics.drawString(font,
                Component.translatable("ui.adventurersguild.gold").getString() + ": " + data.getGold(),
                x + 8, y + 20, DIM_TEXT_COLOR);

        if (rows.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("ui.adventurersguild.shop.empty"),
                    x + SCREEN_WIDTH / 2, y + 114, DIM_TEXT_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int last = Math.min(rows.size(), scrollOffset + maxVisibleRows);
        for (int i = scrollOffset; i < last; i++) {
            Row row = rows.get(i);
            int rowY = y + LIST_TOP + (i - scrollOffset) * ROW_HEIGHT;
            graphics.fill(x + 2, rowY, x + SCREEN_WIDTH - 2, rowY + ROW_HEIGHT - 2, 0x70000000);
            String itemName = font.plainSubstrByWidth(
                    itemName(row.item.getItemId()).getString(), SCREEN_WIDTH - 132);
            graphics.drawString(font, itemName, x + 8, rowY + 3, TEXT_COLOR);
            boolean locked = data.getLevel() < row.item.getMinLevel();
            if (locked) {
                graphics.drawString(font,
                        Component.translatable("ui.adventurersguild.lock_level", row.item.getMinLevel()),
                        x + SCREEN_WIDTH - 100, rowY + 5, LOCK_COLOR);
            } else {
                graphics.drawString(font,
                        row.item.getPrice() + "G  x" + row.item.getCount(),
                        x + SCREEN_WIDTH - 100, rowY + 5, TITLE_COLOR);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static Component itemName(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return Component.literal(itemId);
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item != null && item != Items.AIR ? item.getDescription() : Component.literal(itemId);
    }
}
