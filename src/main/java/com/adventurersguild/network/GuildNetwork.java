package com.adventurersguild.network;

import com.adventurersguild.AdventurersGuild;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/** Mod networking channel. Packets carry requests (C2S) and data snapshots (S2C). */
public final class GuildNetwork {
    public static final String PROTOCOL_VERSION = "2";

    private static SimpleChannel channel;

    private GuildNetwork() {}

    public static void register() {
        channel = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath(AdventurersGuild.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        channel.registerMessage(id++, GuildDataSyncPacket.class,
                GuildDataSyncPacket::encode, GuildDataSyncPacket::decode,
                GuildDataSyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(id++, AcceptQuestPacket.class,
                AcceptQuestPacket::encode, AcceptQuestPacket::decode,
                AcceptQuestPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, AbandonQuestPacket.class,
                AbandonQuestPacket::encode, AbandonQuestPacket::decode,
                AbandonQuestPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, RegisterPlayerPacket.class,
                RegisterPlayerPacket::encode, RegisterPlayerPacket::decode,
                RegisterPlayerPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, BuyItemPacket.class,
                BuyItemPacket::encode, BuyItemPacket::decode,
                BuyItemPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, RefreshQuestPacket.class,
                RefreshQuestPacket::encode, RefreshQuestPacket::decode,
                RefreshQuestPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, DialogueOpenPacket.class,
                DialogueOpenPacket::encode, DialogueOpenPacket::decode,
                DialogueOpenPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(id++, DialogueChoicePacket.class,
                DialogueChoicePacket::encode, DialogueChoicePacket::decode,
                DialogueChoicePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, ChronicleUpdatePacket.class,
                ChronicleUpdatePacket::encode, ChronicleUpdatePacket::decode,
                ChronicleUpdatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(id++, OpenGuildScreenPacket.class,
                OpenGuildScreenPacket::encode, OpenGuildScreenPacket::decode,
                OpenGuildScreenPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, NotificationPacket.class,
                NotificationPacket::encode, NotificationPacket::decode,
                NotificationPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(id++, PartyDataSyncPacket.class,
                PartyDataSyncPacket::encode, PartyDataSyncPacket::decode,
                PartyDataSyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToServer(Object message) {
        channel.sendToServer(message);
    }
}
