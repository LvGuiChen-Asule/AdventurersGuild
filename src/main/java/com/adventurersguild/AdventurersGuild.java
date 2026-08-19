package com.adventurersguild;

import com.adventurersguild.command.AGCommands;
import com.adventurersguild.data.GuildDataLoader;
import com.adventurersguild.entity.npc.GuildNpcEntities;
import com.adventurersguild.entity.npc.GuildNpcEntity;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.npc.GuildNpcHandler;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.quest.QuestManager;
import com.adventurersguild.registry.AGRegistry;
import com.adventurersguild.world.GuildWorldEvents;
import com.adventurersguild.chronicle.ChronicleEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Adventurer's Guild - a quest-driven adventure & progression system mod.
 * <p>
 * V0.1 scope: player registration, quest generation (COLLECT / HUNT),
 * quest accept/execute/complete, gold & EXP rewards, persistence, quest UI.
 * All game state is server-authoritative.
 */
@Mod(AdventurersGuild.MOD_ID)
public class AdventurersGuild {
    public static final String MOD_ID = "adventurersguild";
    public static final String MOD_NAME = "Adventurer's Guild";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public AdventurersGuild() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onRegisterAttributes);
        AGRegistry.register(modBus);
        GuildNpcEntities.register(modBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(AdventurerCapability.class);
        MinecraftForge.EVENT_BUS.register(QuestManager.class);
        MinecraftForge.EVENT_BUS.register(AGCommands.class);
        MinecraftForge.EVENT_BUS.register(GuildDataLoader.class);
        MinecraftForge.EVENT_BUS.register(GuildNpcHandler.class);
        MinecraftForge.EVENT_BUS.register(GuildWorldEvents.class);
        MinecraftForge.EVENT_BUS.register(ChronicleEvents.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(GuildNetwork::register);
    }

    private void onRegisterAttributes(EntityAttributeCreationEvent event) {
        for (net.minecraftforge.registries.RegistryObject<net.minecraft.world.entity.EntityType<GuildNpcEntity>> type
                : GuildNpcEntities.ALL) {
            event.put(type.get(), GuildNpcEntity.createAttributes().build());
        }
    }

    /** Push a fresh data snapshot on login and greet unregistered players. */
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.adventurersguild.player.AdventurerData data =
                    com.adventurersguild.player.AdventurerCapability.get(player);
            if (data != null) {
                data.setPlayerName(player.getGameProfile().getName());
            }
            GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.update(player));
            if (!AdventurerCapability.isRegistered(player)) {
                player.sendSystemMessage(Component.translatable("msg.adventurersguild.welcome_not_registered"));
            }
        }
    }
}
