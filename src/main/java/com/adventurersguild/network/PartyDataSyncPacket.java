package com.adventurersguild.network;

import com.adventurersguild.guild.GuildWorldData;
import com.adventurersguild.party.AdventurerParty;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.player.AdventurerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server -> client party sync (TASK-024). */
public class PartyDataSyncPacket {
    private final String partyId;
    private final String partyName;
    private final String leaderName;
    private final List<String> members;
    private final int level;
    private final int experience;
    private final int completedQuests;
    private final int reputation;

    public PartyDataSyncPacket(String partyId, String partyName, String leaderName,
                               List<String> members, int level, int experience,
                               int completedQuests, int reputation) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.leaderName = leaderName;
        this.members = members;
        this.level = level;
        this.experience = experience;
        this.completedQuests = completedQuests;
        this.reputation = reputation;
    }

    public static PartyDataSyncPacket forPlayer(ServerPlayer player) {
        AdventurerData data = AdventurerCapability.get(player);
        if (data == null || data.getPartyId() == null) {
            return new PartyDataSyncPacket("", "", "", List.of(), 1, 0, 0, 0);
        }
        AdventurerParty party = GuildWorldData.get(player.serverLevel()).getParty(data.getPartyId());
        if (party == null) {
            return new PartyDataSyncPacket("", "", "", List.of(), 1, 0, 0, 0);
        }
        List<String> memberNames = new ArrayList<>();
        for (var uuid : party.getMembers()) {
            ServerPlayer online = player.server.getPlayerList().getPlayer(uuid);
            memberNames.add(online != null ? online.getGameProfile().getName() : uuid.toString().substring(0, 8));
        }
        ServerPlayer leader = player.server.getPlayerList().getPlayer(party.getLeader());
        return new PartyDataSyncPacket(
                party.getPartyId(),
                party.getPartyName(),
                leader != null ? leader.getGameProfile().getName() : "?",
                memberNames,
                party.getLevel(),
                party.getExperience(),
                party.getCompletedQuests(),
                party.getReputation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(partyId);
        buf.writeUtf(partyName);
        buf.writeUtf(leaderName);
        buf.writeCollection(members, FriendlyByteBuf::writeUtf);
        buf.writeVarInt(level);
        buf.writeVarInt(experience);
        buf.writeVarInt(completedQuests);
        buf.writeVarInt(reputation);
    }

    public static PartyDataSyncPacket decode(FriendlyByteBuf buf) {
        String partyId = buf.readUtf(128);
        String partyName = buf.readUtf(128);
        String leaderName = buf.readUtf(128);
        List<String> members = new ArrayList<>(buf.readList(b -> b.readUtf(128)));
        int level = buf.readVarInt();
        int experience = buf.readVarInt();
        int completedQuests = buf.readVarInt();
        int reputation = buf.readVarInt();
        return new PartyDataSyncPacket(partyId, partyName, leaderName, members,
                level, experience, completedQuests, reputation);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> com.adventurersguild.client.ClientPacketHandlers.onPartySync(this)));
        context.setPacketHandled(true);
    }

    public String getPartyId() { return partyId; }
    public String getPartyName() { return partyName; }
    public String getLeaderName() { return leaderName; }
    public List<String> getMembers() { return members; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getCompletedQuests() { return completedQuests; }
    public int getReputation() { return reputation; }
}
