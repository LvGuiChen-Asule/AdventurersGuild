package com.adventurersguild.client;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.client.render.GuildNpcRenderer;
import com.adventurersguild.entity.npc.GuildNpcEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only mod bus events: renderer registration (and later keybinds). */
@Mod.EventBusSubscriber(modid = AdventurersGuild.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        GuildNpcEntities.ALL.forEach(type ->
                event.registerEntityRenderer(type.get(), GuildNpcRenderer::new));
    }
}
