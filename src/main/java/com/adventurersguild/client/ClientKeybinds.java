package com.adventurersguild.client;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.network.OpenGuildScreenPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * TASK-022: keybinds G / J / K / L (rebindable in Controls).
 * Keys only send open-screen requests; all data stays server-authoritative.
 */
public final class ClientKeybinds {
    public static final KeyMapping OPEN_GUILD = key("key.adventurersguild.guild", GLFW.GLFW_KEY_G);
    public static final KeyMapping OPEN_QUESTS = key("key.adventurersguild.quests", GLFW.GLFW_KEY_J);
    public static final KeyMapping OPEN_ADVENTURER = key("key.adventurersguild.adventurer", GLFW.GLFW_KEY_K);
    public static final KeyMapping OPEN_CHRONICLE = key("key.adventurersguild.chronicle", GLFW.GLFW_KEY_L);

    private ClientKeybinds() {}

    private static KeyMapping key(String name, int key) {
        return new KeyMapping(name, InputConstants.Type.KEYSYM, key, "key.adventurersguild.category");
    }

    @Mod.EventBusSubscriber(modid = AdventurersGuild.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_GUILD);
            event.register(OPEN_QUESTS);
            event.register(OPEN_ADVENTURER);
            event.register(OPEN_CHRONICLE);
        }
    }

    @Mod.EventBusSubscriber(modid = AdventurersGuild.MOD_ID, value = Dist.CLIENT)
    public static final class Handler {
        private Handler() {}

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            while (OPEN_GUILD.consumeClick()) {
                GuildNetwork.sendToServer(new OpenGuildScreenPacket("main"));
            }
            while (OPEN_QUESTS.consumeClick()) {
                GuildNetwork.sendToServer(new OpenGuildScreenPacket("quests"));
            }
            while (OPEN_ADVENTURER.consumeClick()) {
                GuildNetwork.sendToServer(new OpenGuildScreenPacket("adventurer"));
            }
            while (OPEN_CHRONICLE.consumeClick()) {
                GuildNetwork.sendToServer(new OpenGuildScreenPacket("chronicle"));
            }
        }
    }
}
