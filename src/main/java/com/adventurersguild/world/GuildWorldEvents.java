package com.adventurersguild.world;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.chronicle.ChronicleManager;
import com.adventurersguild.guild.GuildWorldData;
import com.adventurersguild.npc.GuildNpcManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

/**
 * TASK-005 (V1.1 fix): deterministic surface placement of the guild hall.
 * <p>
 * The jigsaw height projection proved unreliable for our single-piece
 * template (it kept placing the hall at Y=0). The main guild is now placed
 * manually on the world surface near spawn and its real origin is recorded in
 * GuildWorldData. The jigsaw structure registration stays as framework but its
 * structure set was removed so no underground duplicate shells can appear.
 */
public final class GuildWorldEvents {
    /** Search only inside the pre-generated spawn area so no chunk generation is forced. */
    private static final int GUILD_SEARCH_RADIUS_BLOCKS = 96;
    private static final int GUILD_SEARCH_STEP = 8;
    private static final ResourceLocation GUILD_TEMPLATE =
            ResourceLocation.parse("adventurersguild:guild_hall");
    private static final ResourceLocation GUILD_BIOME_TAG =
            ResourceLocation.parse("adventurersguild:has_structure/guild_hall");
    private static final TagKey<Biome> GUILD_BIOMES =
            TagKey.create(Registries.BIOME, GUILD_BIOME_TAG);

    private GuildWorldEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        GuildWorldData worldData = GuildWorldData.get(level);

        if (!worldData.isGuildGenerated()) {
            placeGuild(level, worldData, player);
        }
        GuildNpcManager.ensureNpcsSpawned(level, worldData);
    }

    private static void placeGuild(ServerLevel level, GuildWorldData worldData, ServerPlayer player) {
        BlockPos origin = findPlacementPos(level);
        if (origin == null) {
            AdventurersGuild.LOGGER.warn("[Adventurer's Guild] No suitable surface spot found near spawn");
            return;
        }
        Optional<StructureTemplate> template = level.getStructureManager().get(GUILD_TEMPLATE);
        if (template.isEmpty()) {
            AdventurersGuild.LOGGER.error("[Adventurer's Guild] Guild hall template not found: {}", GUILD_TEMPLATE);
            return;
        }
        clearTerrain(level, origin);
        boolean placed = template.get().placeInWorld(level, origin, origin, new StructurePlaceSettings(),
                level.getRandom(), 2);
        AdventurersGuild.LOGGER.info("[Adventurer's Guild] Guild template size={} placed={}",
                template.get().getSize(), placed);
        if (!placed) {
            AdventurersGuild.LOGGER.error("[Adventurer's Guild] Guild hall placement failed - will retry on next login");
            return;
        }
        placeDoor(level, origin);
        worldData.markGuildGenerated(origin, level.dimension().location().toString());
        worldData.recordWorldEvent(ChronicleManager.EVENT_GUILD_FOUND);
        ChronicleManager.recordEvent(player, ChronicleManager.EVENT_GUILD_FOUND);
        AdventurersGuild.LOGGER.info("[Adventurer's Guild] Guild hall placed at {}", origin);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.guild_found",
                origin.getX(), origin.getY(), origin.getZ()));
    }

    /** Places the entrance door after template placement (stable, no dropped item). */
    private static void placeDoor(ServerLevel level, BlockPos origin) {
        BlockPos door = origin.offset(22, 1, 34);
        level.setBlock(door, Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false), 3);
        level.setBlock(door.above(), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false), 3);
    }

    /**
     * Clears terrain inside the building footprint (so hills/trees cannot poke
     * through) and flattens a 4-block dirt margin around the hall, leaving a
     * clean cleared area around the structure.
     */
    private static void clearTerrain(ServerLevel level, BlockPos origin) {
        int margin = 4;
        int minX = origin.getX() - margin;
        int maxX = origin.getX() + 44 + margin;
        int minZ = origin.getZ() - margin;
        int maxZ = origin.getZ() + 34 + margin;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean inside = x >= origin.getX() && x <= origin.getX() + 44
                        && z >= origin.getZ() && z <= origin.getZ() + 34;
                for (int y = origin.getY(); y <= origin.getY() + 32; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
                if (!inside) {
                    BlockPos ledge = new BlockPos(x, origin.getY() - 1, z);
                    if (!level.getBlockState(ledge.below()).isAir()) {
                        level.setBlock(ledge, Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    /**
     * Finds the first valid surface spot near spawn. Preferred biomes first,
     * any land as fallback, and the spawn point itself as last resort -
     * placement never silently fails.
     */
    private static BlockPos findPlacementPos(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        int seaLevel = level.getSeaLevel();
        int maxRadius = GUILD_SEARCH_RADIUS_BLOCKS / GUILD_SEARCH_STEP;
        BlockPos fallback = null;
        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int x = spawn.getX() + dx * GUILD_SEARCH_STEP;
                    int z = spawn.getZ() + dz * GUILD_SEARCH_STEP;
                    int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                    boolean land = height >= seaLevel - 1
                            && level.getBlockState(new BlockPos(x, height, z)).getBlock() != Blocks.WATER;
                    if (!land) {
                        continue;
                    }
                    if (level.getBiome(new BlockPos(x, 0, z)).is(GUILD_BIOMES)) {
                        return new BlockPos(x, height, z);
                    }
                    if (fallback == null) {
                        fallback = new BlockPos(x, height, z);
                    }
                }
            }
        }
        if (fallback != null) {
            AdventurersGuild.LOGGER.warn("[Adventurer's Guild] No preferred biome found; using fallback land spot {}", fallback);
            return fallback;
        }
        int spawnHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, spawn.getX(), spawn.getZ());
        String spawnBiome = level.getBiome(spawn).unwrapKey()
                .map(key -> key.location().toString()).orElse("unknown");
        AdventurersGuild.LOGGER.warn("[Adventurer's Guild] Placing guild at spawn {} (biome {})",
                spawn, spawnBiome);
        return new BlockPos(spawn.getX(), Math.max(spawnHeight, seaLevel), spawn.getZ());
    }
}
