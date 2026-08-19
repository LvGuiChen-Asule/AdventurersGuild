package com.adventurersguild.entity.npc;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.npc.GuildNpcHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** TASK-007: registers the six guild NPC entity types. */
public final class GuildNpcEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AdventurersGuild.MOD_ID);

    public static final RegistryObject<EntityType<GuildNpcEntity>> RECEPTIONIST =
            register(GuildNpcHandler.ROLE_RECEPTIONIST);
    public static final RegistryObject<EntityType<GuildNpcEntity>> QUEST_MASTER =
            register(GuildNpcHandler.ROLE_QUEST_MASTER);
    public static final RegistryObject<EntityType<GuildNpcEntity>> SHOPKEEPER =
            register(GuildNpcHandler.ROLE_SHOPKEEPER);
    public static final RegistryObject<EntityType<GuildNpcEntity>> EXPEDITION_MASTER =
            register(GuildNpcHandler.ROLE_EXPEDITION_MASTER);
    public static final RegistryObject<EntityType<GuildNpcEntity>> ARCHIVIST =
            register(GuildNpcHandler.ROLE_ARCHIVIST);
    public static final RegistryObject<EntityType<GuildNpcEntity>> END_RESEARCHER =
            register(GuildNpcHandler.ROLE_END_RESEARCHER);

    public static final List<RegistryObject<EntityType<GuildNpcEntity>>> ALL = List.of(
            RECEPTIONIST, QUEST_MASTER, SHOPKEEPER, EXPEDITION_MASTER, ARCHIVIST, END_RESEARCHER);

    private GuildNpcEntities() {}

    private static RegistryObject<EntityType<GuildNpcEntity>> register(String role) {
        return ENTITY_TYPES.register(role, () -> EntityType.Builder.<GuildNpcEntity>of(
                        (type, level) -> new GuildNpcEntity(type, level, role), MobCategory.MISC)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(10)
                .build(role));
    }

    public static EntityType<GuildNpcEntity> typeFor(String role) {
        return switch (role) {
            case GuildNpcHandler.ROLE_RECEPTIONIST -> RECEPTIONIST.get();
            case GuildNpcHandler.ROLE_QUEST_MASTER -> QUEST_MASTER.get();
            case GuildNpcHandler.ROLE_SHOPKEEPER -> SHOPKEEPER.get();
            case GuildNpcHandler.ROLE_EXPEDITION_MASTER -> EXPEDITION_MASTER.get();
            case GuildNpcHandler.ROLE_ARCHIVIST -> ARCHIVIST.get();
            case GuildNpcHandler.ROLE_END_RESEARCHER -> END_RESEARCHER.get();
            default -> RECEPTIONIST.get();
        };
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
