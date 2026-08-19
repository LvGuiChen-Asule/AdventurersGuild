package com.adventurersguild.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

/**
 * TASK-008: custom NPC renderer. Reuses the vanilla villager model/textures
 * (profession-based, one distinct look per NPC role) as a stable baseline;
 * custom skin textures can be layered on later without changing the entity.
 */
public class GuildNpcRenderer extends VillagerRenderer {
    public GuildNpcRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Villager entity) {
        return super.getTextureLocation(entity);
    }
}
