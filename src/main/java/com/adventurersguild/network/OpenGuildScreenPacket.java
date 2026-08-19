package com.adventurersguild.network;

import com.adventurersguild.chronicle.ChronicleManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: request to open a guild screen (keybinds / UI shortcuts). */
public class OpenGuildScreenPacket {
    private final String screen;

    public OpenGuildScreenPacket(String screen) {
        this.screen = screen;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(screen, 32);
    }

    public static OpenGuildScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenGuildScreenPacket(buf.readUtf(32));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            switch (screen) {
                case "main" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.main(player));
                case "quests" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.hall(player));
                case "myquests" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.myQuests(player));
                case "adventurer" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.adventurer(player));
                case "shop" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.shop(player));
                case "chains" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.chains(player));
                case "chronicle" -> GuildNetwork.sendToPlayer(player, ChronicleUpdatePacket.forPlayer(player, true));
                default -> { }
            }
        });
        context.setPacketHandled(true);
    }
}
