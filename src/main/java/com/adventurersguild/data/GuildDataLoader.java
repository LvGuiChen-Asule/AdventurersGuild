package com.adventurersguild.data;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Registers all data-driven reload listeners (quests/pools/chains/shops/equipment). */
public final class GuildDataLoader {
    private GuildDataLoader() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new QuestRegistry.ReloadListener());
        event.addListener(new QuestPoolRegistry.ReloadListener());
        event.addListener(new QuestChainRegistry.ReloadListener());
        event.addListener(new ShopRegistry.ReloadListener());
        event.addListener(new EquipmentRegistry.ReloadListener());
        event.addListener(new DialogueRegistry.ReloadListener());
        event.addListener(new com.adventurersguild.chapter.ChapterRegistry.ReloadListener());
        event.addListener(new LoreRegistry.ReloadListener());
    }
}
