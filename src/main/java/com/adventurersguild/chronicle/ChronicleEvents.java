package com.adventurersguild.chronicle;

import com.adventurersguild.guild.GuildWorldData;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.ChronicleState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * TASK-012: the 15 world events. All are once=true, recorded server-side,
 * and fire regardless of whether the player has the matching quest active
 * (weak linearity - the mod records what the player actually did).
 */
public final class ChronicleEvents {
    private static final ResourceKey<Structure> FORTRESS_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocation.parse("minecraft:fortress"));
    private static final ResourceKey<Structure> STRONGHOLD_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocation.parse("minecraft:stronghold"));

    private ChronicleEvents() {}

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ResourceKey<Level> to = event.getTo();
        if (to.equals(Level.NETHER)) {
            ChronicleManager.recordEvent(player, ChronicleManager.EVENT_FIRST_NETHER);
        } else if (to.equals(Level.END)) {
            ChronicleManager.recordEvent(player, ChronicleManager.EVENT_END_PORTAL_OPEN);
            ChronicleManager.recordEvent(player, ChronicleManager.EVENT_FIRST_END);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ChronicleManager.recordEvent(player, ChronicleManager.EVENT_FIRST_DEATH);
        }
        if (event.getEntity() instanceof EnderDragon
                && event.getSource().getEntity() instanceof ServerPlayer killer) {
            ChronicleManager.recordEvent(killer, ChronicleManager.EVENT_DRAGON_DEATH);
            ChronicleState state = AdventurerCapability.getChronicleState(killer);
            if (state != null) {
                state.incrementCounter("dragonKills", 1);
            }
            ChronicleManager.syncChronicle(killer);
            GuildWorldData.get(killer.serverLevel()).recordWorldEvent(ChronicleManager.EVENT_DRAGON_DEATH);
        }
        if (event.getEntity() instanceof WitherBoss
                && event.getSource().getEntity() instanceof ServerPlayer killer) {
            ChronicleManager.recordEvent(killer, ChronicleManager.EVENT_WITHER_DEATH);
            ChronicleState state = AdventurerCapability.getChronicleState(killer);
            if (state != null) {
                state.incrementCounter("witherKills", 1);
            }
            ChronicleManager.syncChronicle(killer);
            GuildWorldData.get(killer.serverLevel()).recordWorldEvent(ChronicleManager.EVENT_WITHER_DEATH);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof WitherBoss && event.getLevel() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                ChronicleManager.recordEvent(player, ChronicleManager.EVENT_WITHER_SUMMON);
            }
            GuildWorldData.get(level).recordWorldEvent(ChronicleManager.EVENT_WITHER_SUMMON);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().getEntity() instanceof EnderDragon) {
            ChronicleManager.recordEvent(player, ChronicleManager.EVENT_FIRST_DRAGON_ATTACK);
        }
        if (event.getEntity() instanceof EnderDragon
                && event.getSource().getEntity() instanceof ServerPlayer attacker) {
            ChronicleState state = AdventurerCapability.getChronicleState(attacker);
            if (state != null) {
                state.incrementCounter("dragonDamage", 1);
                ChronicleManager.syncChronicle(attacker);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EndCrystal && event.getLevel() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                ChronicleState state = AdventurerCapability.getChronicleState(player);
                if (state != null) {
                    state.incrementCounter("endCrystals", 1);
                    ChronicleManager.syncChronicle(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getCrafting().is(Items.ENDER_EYE)) {
            ChronicleManager.recordEvent(player, ChronicleManager.EVENT_FIRST_ENDER_EYE);
        }
    }

    /** Low-frequency structure/biome checks (every 5 seconds, per player). */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 100 != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level.dimension().equals(Level.NETHER)) {
            StructureStart fortress = level.structureManager()
                    .getStructureWithPieceAt(player.blockPosition(), FORTRESS_KEY);
            if (fortress != null && fortress.isValid()) {
                ChronicleManager.recordEvent(player, ChronicleManager.EVENT_NETHER_FORTRESS);
            }
        } else if (level.dimension().equals(Level.OVERWORLD)) {
            StructureStart stronghold = level.structureManager()
                    .getStructureWithPieceAt(player.blockPosition(), STRONGHOLD_KEY);
            if (stronghold != null && stronghold.isValid()) {
                ChronicleManager.recordEvent(player, ChronicleManager.EVENT_STRONGHOLD_FOUND);
            }
        } else if (level.dimension().equals(Level.END)) {
            BlockPos pos = player.blockPosition();
            if (Math.abs(pos.getX()) > 800 || Math.abs(pos.getZ()) > 800) {
                ChronicleManager.recordEvent(player, ChronicleManager.EVENT_END_ISLAND);
            }
        }
    }
}
