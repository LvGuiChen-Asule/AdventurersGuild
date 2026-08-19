package com.adventurersguild.party;

import com.adventurersguild.guild.GuildWorldData;
import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.network.PartyDataSyncPacket;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.AdventurerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TASK-019: server-authoritative adventurer party manager.
 * Create / invite / join / leave / disband only (no AI teammates, no classes,
 * no combat sync in V1.1).
 */
public final class PartyManager {
    private static final Map<UUID, String> PENDING_INVITES = new HashMap<>();

    private PartyManager() {}

    public static String create(ServerPlayer player, String name) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null) {
            return null;
        }
        if (data.getPartyId() != null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_already"));
            return null;
        }
        String partyId = "party_" + UUID.randomUUID().toString().substring(0, 8);
        AdventurerParty party = new AdventurerParty(
                partyId, player.getUUID(), name, player.serverLevel().getGameTime());
        GuildWorldData.get(player.serverLevel()).putParty(party);
        data.setPartyId(partyId);
        player.sendSystemMessage(Component.translatable(
                "msg.adventurersguild.party_created", name).withStyle(ChatFormatting.GOLD));
        syncParty(player, party);
        return partyId;
    }

    public static boolean invite(ServerPlayer player, ServerPlayer target) {
        AdventurerParty party = getParty(player);
        if (party == null || !party.isLeader(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_need_leader"));
            return false;
        }
        AdventurerData targetData = AdventurerCapability.get(target);
        if (targetData == null || targetData.getPartyId() != null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_target_in_party"));
            return false;
        }
        PENDING_INVITES.put(target.getUUID(), party.getPartyId());
        player.sendSystemMessage(Component.translatable(
                "msg.adventurersguild.party_invite_sent", target.getGameProfile().getName()));
        target.sendSystemMessage(Component.translatable(
                "msg.adventurersguild.party_invited", party.getPartyName()));
        return true;
    }

    public static boolean accept(ServerPlayer player) {
        String partyId = PENDING_INVITES.remove(player.getUUID());
        if (partyId == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_no_invite"));
            return false;
        }
        return join(player, partyId);
    }

    public static boolean join(ServerPlayer player, String partyId) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || data.getPartyId() != null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_already"));
            return false;
        }
        AdventurerParty party = GuildWorldData.get(player.serverLevel()).getParty(partyId);
        if (party == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_not_found"));
            return false;
        }
        party.addMember(player.getUUID());
        data.setPartyId(partyId);
        player.sendSystemMessage(Component.translatable(
                "msg.adventurersguild.party_joined", party.getPartyName()).withStyle(ChatFormatting.GREEN));
        syncParty(player, party);
        return true;
    }

    public static boolean leave(ServerPlayer player) {
        AdventurerParty party = getParty(player);
        if (party == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_not_in"));
            return false;
        }
        if (party.isLeader(player.getUUID())) {
            return disband(player);
        }
        party.removeMember(player.getUUID());
        AdventurerData data = AdventurerCapability.get(player);
        if (data != null) {
            data.setPartyId(null);
        }
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_left"));
        syncParty(player, party);
        return true;
    }

    public static boolean disband(ServerPlayer player) {
        AdventurerParty party = getParty(player);
        if (party == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_not_in"));
            return false;
        }
        if (!party.isLeader(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_need_leader"));
            return false;
        }
        for (UUID member : party.getMembers()) {
            ServerPlayer online = player.server.getPlayerList().getPlayer(member);
            if (online != null) {
                AdventurerData data = AdventurerCapability.get(online);
                if (data != null) {
                    data.setPartyId(null);
                }
                online.sendSystemMessage(Component.translatable(
                        "msg.adventurersguild.party_disbanded", party.getPartyName()));
            }
        }
        GuildWorldData.get(player.serverLevel()).removeParty(party.getPartyId());
        return true;
    }

    public static void info(ServerPlayer player) {
        AdventurerParty party = getParty(player);
        if (party == null) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_not_in"));
            return;
        }
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.party.info_header",
                party.getPartyName()).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.party.leader",
                playerName(player, party.getLeader())));
        StringBuilder members = new StringBuilder();
        for (UUID member : party.getMembers()) {
            if (members.length() > 0) {
                members.append(", ");
            }
            members.append(playerName(player, member));
        }
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.party.members", members.toString()));
        player.sendSystemMessage(Component.translatable("cmd.adventurersguild.party.stats",
                party.getLevel(), party.getExperience(), party.getCompletedQuests(), party.getReputation()));
    }

    public static void onQuestCompleted(ServerPlayer player) {
        AdventurerParty party = getParty(player);
        if (party == null) {
            return;
        }
        party.addExperience(10);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.party_exp", 10));
        syncParty(player, party);
    }

    private static AdventurerParty getParty(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || data.getPartyId() == null) {
            return null;
        }
        return GuildWorldData.get(player.serverLevel()).getParty(data.getPartyId());
    }

    private static String playerName(ServerPlayer context, UUID uuid) {
        ServerPlayer online = context.server.getPlayerList().getPlayer(uuid);
        return online != null ? online.getGameProfile().getName() : uuid.toString().substring(0, 8);
    }

    private static void syncParty(ServerPlayer context, AdventurerParty party) {
        for (UUID member : party.getMembers()) {
            ServerPlayer online = context.server.getPlayerList().getPlayer(member);
            if (online != null) {
                GuildNetwork.sendToPlayer(online, PartyDataSyncPacket.forPlayer(online));
                GuildNetwork.sendToPlayer(online, GuildDataSyncPacket.update(online));
            }
        }
    }
}
