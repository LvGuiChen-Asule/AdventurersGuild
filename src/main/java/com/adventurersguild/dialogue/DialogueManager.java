package com.adventurersguild.dialogue;

import com.adventurersguild.chronicle.ChronicleManager;
import com.adventurersguild.data.DialogueRegistry;
import com.adventurersguild.network.ChronicleUpdatePacket;
import com.adventurersguild.network.DialogueOpenPacket;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.AdventurerData;
import com.adventurersguild.player.ChronicleState;
import com.adventurersguild.player.UnlockState;
import com.adventurersguild.quest.QuestManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * TASK-010: server-authoritative dialogue manager.
 * Clients only see the current node's text + available choices; conditions
 * are evaluated and actions are executed on the server.
 */
public final class DialogueManager {
    private DialogueManager() {}

    public static void openDialogue(ServerPlayer player, String npcRole) {
        ChronicleState chronicle = AdventurerCapability.getChronicleState(player);
        if (chronicle != null) {
            chronicle.incrementCounter("visit." + npcRole, 1);
        }
        Dialogue dialogue = DialogueRegistry.getForNpc(npcRole);
        if (dialogue == null) {
            player.sendSystemMessage(Component.translatable("dialogue.adventurersguild.default"));
            return;
        }
        sendNode(player, dialogue, dialogue.getStartNode());
    }

    public static void onChoice(ServerPlayer player, String dialogueId, String nodeId, int choiceIndex) {
        Dialogue dialogue = DialogueRegistry.get(dialogueId);
        if (dialogue == null) {
            return;
        }
        DialogueNode node = dialogue.getNode(nodeId);
        if (node == null) {
            return;
        }
        List<DialogueChoice> available = availableChoices(player, node);
        if (choiceIndex < 0 || choiceIndex >= available.size()) {
            return;
        }
        DialogueChoice choice = available.get(choiceIndex);
        for (DialogueAction action : choice.getActions()) {
            runAction(player, action);
        }
        if ("close".equals(choice.getNext())) {
            GuildNetwork.sendToPlayer(player, new DialogueOpenPacket(
                    dialogue.getId(), dialogue.getNpc(), "close", "", List.of()));
        } else {
            sendNode(player, dialogue, choice.getNext());
        }
    }

    private static void sendNode(ServerPlayer player, Dialogue dialogue, String nodeId) {
        DialogueNode node = dialogue.getNode(nodeId);
        if (node == null) {
            GuildNetwork.sendToPlayer(player, new DialogueOpenPacket(
                    dialogue.getId(), dialogue.getNpc(), "close", "", List.of()));
            return;
        }
        List<String> choiceKeys = new ArrayList<>();
        for (DialogueChoice choice : node.getChoices()) {
            if (allConditionsMet(player, choice.getConditions())) {
                choiceKeys.add(choice.getTextKey());
            }
        }
        GuildNetwork.sendToPlayer(player, new DialogueOpenPacket(
                dialogue.getId(), dialogue.getNpc(), node.getId(), node.getTextKey(), choiceKeys));
    }

    private static List<DialogueChoice> availableChoices(ServerPlayer player, DialogueNode node) {
        List<DialogueChoice> available = new ArrayList<>();
        for (DialogueChoice choice : node.getChoices()) {
            if (allConditionsMet(player, choice.getConditions())) {
                available.add(choice);
            }
        }
        return available;
    }

    private static boolean allConditionsMet(ServerPlayer player, List<DialogueCondition> conditions) {
        for (DialogueCondition condition : conditions) {
            if (!condition.met(player)) {
                return false;
            }
        }
        return true;
    }

    private static void runAction(ServerPlayer player, DialogueAction action) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return;
        }
        switch (action.getType()) {
            case "unlock" -> {
                UnlockState unlocks = AdventurerCapability.getUnlockState(player);
                unlocks.unlock(action.getValue());
                sync(player);
            }
            case "start_quest" -> QuestManager.acceptQuest(player, action.getValue());
            case "register" -> QuestManager.register(player);
            case "open_screen" -> openScreen(player, action.getValue());
            case "record_event" -> ChronicleManager.recordEvent(player, action.getValue());
            case "discover_lore" -> {
                AdventurerCapability.getChronicleState(player).discoverLore(action.getValue());
                sync(player);
            }
            case "give_reward" -> giveReward(player, action.getValue(), action.getAmount());
            default -> { }
        }
    }

    private static void openScreen(ServerPlayer player, String screen) {
        switch (screen) {
            case "quests" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.hall(player));
            case "myquests" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.myQuests(player));
            case "adventurer" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.adventurer(player));
            case "shop" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.shop(player));
            case "chains" -> GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.chains(player));
            case "chronicle" -> GuildNetwork.sendToPlayer(player, ChronicleUpdatePacket.forPlayer(player, true));
            default -> { }
        }
    }

    private static void giveReward(ServerPlayer player, String key, int amount) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || amount <= 0) {
            return;
        }
        switch (key) {
            case "gold" -> {
                data.addGold(amount);
                player.sendSystemMessage(Component.translatable(
                        "msg.adventurersguild.reward_gold", amount).withStyle(ChatFormatting.GOLD));
            }
            case "exp" -> {
                data.addExperience(amount);
                player.sendSystemMessage(Component.translatable(
                        "msg.adventurersguild.reward_exp", amount).withStyle(ChatFormatting.AQUA));
            }
            case "reputation" -> {
                data.addReputation(amount);
                player.sendSystemMessage(Component.translatable(
                        "msg.adventurersguild.reward_reputation", amount).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            default -> { }
        }
        sync(player);
    }

    private static void sync(ServerPlayer player) {
        GuildNetwork.sendToPlayer(player, GuildDataSyncPacket.update(player));
    }
}
