package com.adventurersguild.npc;

import com.adventurersguild.guild.GuildWorldData;
import com.adventurersguild.entity.npc.GuildNpcEntities;
import com.adventurersguild.entity.npc.GuildNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TASK-005: spawns the guild NPCs once per world at the recorded guild hall.
 * TASK-006/007 will replace the tagged-villager body with GuildNPCEntity;
 * the stand positions match the carpet markers inside guild_hall.nbt.
 */
public final class GuildNpcManager {
    public static final Map<String, int[]> NPC_STANDS = new LinkedHashMap<>();

    static {
        NPC_STANDS.put(GuildNpcHandler.ROLE_RECEPTIONIST, new int[]{23, 2, 22});
        NPC_STANDS.put(GuildNpcHandler.ROLE_QUEST_MASTER, new int[]{36, 2, 16});
        NPC_STANDS.put(GuildNpcHandler.ROLE_SHOPKEEPER, new int[]{34, 2, 25});
        NPC_STANDS.put(GuildNpcHandler.ROLE_EXPEDITION_MASTER, new int[]{10, 2, 8});
        NPC_STANDS.put(GuildNpcHandler.ROLE_ARCHIVIST, new int[]{10, 2, 26});
        NPC_STANDS.put(GuildNpcHandler.ROLE_END_RESEARCHER, new int[]{18, 2, 8});
    }

    private GuildNpcManager() {}

    /** Spawns all six NPCs exactly once per world. */
    public static void ensureNpcsSpawned(ServerLevel level, GuildWorldData worldData) {
        if (worldData.areNpcsSpawned()) {
            return;
        }
        if (!worldData.isGuildGenerated()) {
            return;
        }
        BlockPos locatePos = worldData.getGuildPosition();
        removeOldNpcs(level, locatePos);
        Map<String, BlockPos> stands = findMarkerStands(level, locatePos);
        for (String role : NPC_STANDS.keySet()) {
            BlockPos stand = stands.get(role);
            if (stand == null) {
                int[] offset = NPC_STANDS.get(role);
                // Offsets are relative to the template origin; the floor sits at origin.y.
                stand = new BlockPos(
                        locatePos.getX() + offset[0],
                        locatePos.getY() + offset[1],
                        locatePos.getZ() + offset[2]);
            } else {
                stand = stand.above();
            }
            spawnNpcAt(level, stand, role);
        }
        worldData.markNpcsSpawned();
    }

    /** Removes previously spawned guild NPCs around the hall (re-entry safe). */
    private static void removeOldNpcs(ServerLevel level, BlockPos origin) {
        AABB area = new AABB(
                origin.getX() - 64, origin.getY() - 16, origin.getZ() - 64,
                origin.getX() + 64, origin.getY() + 32, origin.getZ() + 64);
        for (GuildNpcEntity npc : level.getEntitiesOfClass(GuildNpcEntity.class, area)) {
            npc.discard();
        }
    }

    /**
     * Scans the placed guild hall bounding box for the role carpet markers
     * (rotation- and height-safe, unlike fixed offsets).
     */
    private static Map<String, BlockPos> findMarkerStands(ServerLevel level, BlockPos locatePos) {
        Map<String, Block> markers = new LinkedHashMap<>();
        markers.put(GuildNpcHandler.ROLE_RECEPTIONIST, Blocks.WHITE_CARPET);
        markers.put(GuildNpcHandler.ROLE_QUEST_MASTER, Blocks.YELLOW_CARPET);
        markers.put(GuildNpcHandler.ROLE_SHOPKEEPER, Blocks.ORANGE_CARPET);
        markers.put(GuildNpcHandler.ROLE_EXPEDITION_MASTER, Blocks.BLUE_CARPET);
        markers.put(GuildNpcHandler.ROLE_ARCHIVIST, Blocks.PURPLE_CARPET);
        markers.put(GuildNpcHandler.ROLE_END_RESEARCHER, Blocks.CYAN_CARPET);

        Map<String, BlockPos> found = new LinkedHashMap<>();
        BoundingBox box = findGuildBox(level, locatePos);
        if (box == null) {
            return found;
        }
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            Block block = level.getBlockState(pos).getBlock();
            for (Map.Entry<String, Block> entry : markers.entrySet()) {
                if (block == entry.getValue() && !found.containsKey(entry.getKey())) {
                    found.put(entry.getKey(), pos.immutable());
                }
            }
            if (found.size() == markers.size()) {
                break;
            }
        }
        return found;
    }

    private static BoundingBox findGuildBox(ServerLevel level, BlockPos locatePos) {
        ResourceKey<Structure> key = ResourceKey.create(
                Registries.STRUCTURE, ResourceLocation.parse("adventurersguild:guild_hall"));
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(key);
        if (structure == null) {
            return null;
        }
        StructureStart start = level.structureManager().getStructureWithPieceAt(locatePos, structure);
        if (start != null && start.isValid()) {
            return start.getBoundingBox();
        }
        // Manual placement has no structure start; scan a box around the recorded origin.
        return new BoundingBox(
                locatePos.getX() - 60, locatePos.getY() - 5, locatePos.getZ() - 60,
                locatePos.getX() + 60, locatePos.getY() + 30, locatePos.getZ() + 60);
    }

    public static GuildNpcEntity spawnNpcAt(ServerLevel level, BlockPos pos, String role) {
        GuildNpcEntity npc = new GuildNpcEntity(GuildNpcEntities.typeFor(role), level, role);
        npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        npc.setHomePosition(pos);
        npc.setCustomName(Component.translatable("npc.adventurersguild." + role));
        npc.setCustomNameVisible(true);
        level.addFreshEntity(npc);
        return npc;
    }
}
