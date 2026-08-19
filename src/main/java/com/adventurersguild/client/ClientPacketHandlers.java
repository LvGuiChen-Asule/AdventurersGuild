package com.adventurersguild.client;

import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.DialogueOpenPacket;
import com.adventurersguild.network.ChronicleUpdatePacket;
import com.adventurersguild.network.NotificationPacket;
import com.adventurersguild.network.PartyDataSyncPacket;
import com.adventurersguild.client.screen.DialogueScreen;
import com.adventurersguild.client.screen.ChronicleScreen;
import com.adventurersguild.client.screen.GuildMainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Client-side packet handling: stores data and opens/refreshes guild screens. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {}

    public static void onGuildData(GuildDataSyncPacket packet) {
        ClientGuildData.update(packet);
        Minecraft minecraft = Minecraft.getInstance();
        switch (packet.getScreen()) {
            case HALL -> minecraft.setScreen(new QuestBoardScreen(packet));
            case MY_QUESTS -> minecraft.setScreen(new ActiveQuestScreen(packet));
            case ADVENTURER -> minecraft.setScreen(new AdventurerScreen(packet));
            case SHOP -> minecraft.setScreen(new ShopScreen(packet));
            case CHAINS -> minecraft.setScreen(new QuestChainScreen(packet));
            case MAIN -> minecraft.setScreen(new GuildMainScreen(packet));
            case UPDATE -> {
                Screen current = minecraft.screen;
                if (current instanceof QuestDetailScreen) {
                    // A quest was just accepted; return to the (refreshed) hall.
                    minecraft.setScreen(new QuestBoardScreen(packet));
                } else if (current instanceof AbstractGuildScreen guildScreen) {
                    guildScreen.refresh(packet);
                }
            }
        }
    }

    public static void onDialogueOpen(DialogueOpenPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if ("close".equals(packet.getNodeId())) {
            if (minecraft.screen instanceof DialogueScreen) {
                minecraft.setScreen(null);
            }
            return;
        }
        if (minecraft.screen instanceof DialogueScreen dialogueScreen) {
            dialogueScreen.refreshNode(packet);
        } else {
            minecraft.setScreen(new DialogueScreen(packet));
        }
    }

    public static void onChronicleUpdate(ChronicleUpdatePacket packet) {
        ClientChronicleData.update(packet);
        if (packet.shouldOpenScreen()) {
            Minecraft.getInstance().setScreen(new ChronicleScreen(packet));
        }
    }

    public static void onNotification(NotificationPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(net.minecraft.network.chat.Component.literal(packet.getMessage()), false);
        }
    }

    public static void onPartySync(PartyDataSyncPacket packet) {
        ClientPartyData.update(packet);
        if (Minecraft.getInstance().screen instanceof com.adventurersguild.client.screen.PartyScreen partyScreen) {
            partyScreen.refresh(packet);
        }
    }
}
