package com.adventurersguild.party;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TASK-019 data model: an adventurer party. Lives in GuildWorldData
 * (world-level, server-authoritative). V1.1 keeps it lightweight:
 * create / invite / join / leave / disband only.
 */
public class AdventurerParty {
    private final String partyId;
    private UUID leader;
    private String partyName;
    private final List<UUID> members = new ArrayList<>();
    private int level = 1;
    private int experience;
    private int completedQuests;
    private int reputation;
    private long creationTime;

    public AdventurerParty(String partyId, UUID leader, String partyName, long creationTime) {
        this.partyId = partyId;
        this.leader = leader;
        this.partyName = partyName;
        this.creationTime = creationTime;
        this.members.add(leader);
    }

    public String getPartyId() { return partyId; }
    public UUID getLeader() { return leader; }
    public String getPartyName() { return partyName; }
    public List<UUID> getMembers() { return members; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getCompletedQuests() { return completedQuests; }
    public int getReputation() { return reputation; }
    public long getCreationTime() { return creationTime; }

    public boolean isMember(UUID uuid) { return members.contains(uuid); }
    public boolean isLeader(UUID uuid) { return leader != null && leader.equals(uuid); }

    public void setLeader(UUID leader) { this.leader = leader; }
    public void setPartyName(String partyName) { this.partyName = partyName; }
    public void addMember(UUID uuid) {
        if (!members.contains(uuid)) {
            members.add(uuid);
        }
    }
    public boolean removeMember(UUID uuid) {
        return members.remove(uuid);
    }
    public void addExperience(int amount) {
        experience = Math.max(0, experience + amount);
        level = Math.max(1, level + (experience >= level * 500 ? 1 : 0));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("partyId", partyId);
        tag.putUUID("leader", leader);
        tag.putString("partyName", partyName == null ? "" : partyName);
        tag.putInt("level", level);
        tag.putInt("experience", experience);
        tag.putInt("completedQuests", completedQuests);
        tag.putInt("reputation", reputation);
        tag.putLong("creationTime", creationTime);
        ListTag memberList = new ListTag();
        for (UUID uuid : members) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            memberList.add(entry);
        }
        tag.put("members", memberList);
        return tag;
    }

    public static AdventurerParty load(CompoundTag tag) {
        AdventurerParty party = new AdventurerParty(
                tag.getString("partyId"),
                tag.getUUID("leader"),
                tag.getString("partyName"),
                tag.getLong("creationTime"));
        party.level = tag.getInt("level");
        party.experience = tag.getInt("experience");
        party.completedQuests = tag.getInt("completedQuests");
        party.reputation = tag.getInt("reputation");
        party.members.clear();
        ListTag memberList = tag.getList("members", Tag.TAG_COMPOUND);
        for (int i = 0; i < memberList.size(); i++) {
            party.members.add(memberList.getCompound(i).getUUID("uuid"));
        }
        return party;
    }
}
