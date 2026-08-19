package com.adventurersguild.guild;

import com.adventurersguild.party.AdventurerParty;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * TASK-003: world-level guild data (SavedData).
 * Tracks the main guild position, world events, global lore and party registry.
 * Survives world restarts.
 */
public class GuildWorldData extends SavedData {
    public static final String DATA_KEY = "adventurersguild_world";

    private boolean guildGenerated;
    private int guildX;
    private int guildY;
    private int guildZ;
    private String guildDimension = "minecraft:overworld";
    private int guildLevel = 1;
    private boolean npcsSpawned;
    private final Set<String> worldEvents = new LinkedHashSet<>();
    private final Set<String> globalLore = new LinkedHashSet<>();
    private final Map<String, AdventurerParty> parties = new LinkedHashMap<>();

    public static GuildWorldData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                GuildWorldData::load, GuildWorldData::new, DATA_KEY);
    }

    public static GuildWorldData load(CompoundTag tag) {
        GuildWorldData data = new GuildWorldData();
        data.guildGenerated = tag.getBoolean("guildGenerated");
        data.guildX = tag.getInt("guildX");
        data.guildY = tag.getInt("guildY");
        data.guildZ = tag.getInt("guildZ");
        data.guildDimension = tag.getString("guildDimension");
        data.guildLevel = tag.getInt("guildLevel");
        data.npcsSpawned = tag.getBoolean("npcsSpawned");
        ListTag events = tag.getList("worldEvents", Tag.TAG_COMPOUND);
        for (int i = 0; i < events.size(); i++) {
            data.worldEvents.add(events.getCompound(i).getString("id"));
        }
        ListTag lore = tag.getList("globalLore", Tag.TAG_COMPOUND);
        for (int i = 0; i < lore.size(); i++) {
            data.globalLore.add(lore.getCompound(i).getString("id"));
        }
        ListTag partyList = tag.getList("parties", Tag.TAG_COMPOUND);
        for (int i = 0; i < partyList.size(); i++) {
            AdventurerParty party = AdventurerParty.load(partyList.getCompound(i));
            data.parties.put(party.getPartyId(), party);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("guildGenerated", guildGenerated);
        tag.putInt("guildX", guildX);
        tag.putInt("guildY", guildY);
        tag.putInt("guildZ", guildZ);
        tag.putString("guildDimension", guildDimension);
        tag.putInt("guildLevel", guildLevel);
        tag.putBoolean("npcsSpawned", npcsSpawned);
        ListTag events = new ListTag();
        for (String eventId : worldEvents) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", eventId);
            events.add(entry);
        }
        tag.put("worldEvents", events);
        ListTag lore = new ListTag();
        for (String loreId : globalLore) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", loreId);
            lore.add(entry);
        }
        tag.put("globalLore", lore);
        ListTag partyList = new ListTag();
        for (AdventurerParty party : parties.values()) {
            partyList.add(party.save());
        }
        tag.put("parties", partyList);
        return tag;
    }

    // ---------- guild position ----------

    public boolean isGuildGenerated() { return guildGenerated; }

    public void markGuildGenerated(BlockPos pos, String dimension) {
        this.guildGenerated = true;
        this.guildX = pos.getX();
        this.guildY = pos.getY();
        this.guildZ = pos.getZ();
        this.guildDimension = dimension;
        setDirty();
    }

    /** Debug: clears the recorded guild so it can be re-located (dev only). */
    public void clearGuild() {
        this.guildGenerated = false;
        this.npcsSpawned = false;
        this.guildX = 0;
        this.guildY = 0;
        this.guildZ = 0;
        setDirty();
    }

    public BlockPos getGuildPosition() {
        return new BlockPos(guildX, guildY, guildZ);
    }

    public String getGuildDimension() { return guildDimension; }

    public int getGuildLevel() { return guildLevel; }
    public void setGuildLevel(int guildLevel) {
        this.guildLevel = guildLevel;
        setDirty();
    }

    public boolean areNpcsSpawned() { return npcsSpawned; }
    public void markNpcsSpawned() {
        this.npcsSpawned = true;
        setDirty();
    }

    // ---------- world events / global lore ----------

    public Set<String> getWorldEvents() { return worldEvents; }
    public boolean hasWorldEvent(String eventId) { return worldEvents.contains(eventId); }
    public void recordWorldEvent(String eventId) {
        if (worldEvents.add(eventId)) {
            setDirty();
        }
    }

    public Set<String> getGlobalLore() { return globalLore; }
    public boolean hasGlobalLore(String loreId) { return globalLore.contains(loreId); }
    public void discoverGlobalLore(String loreId) {
        if (globalLore.add(loreId)) {
            setDirty();
        }
    }

    // ---------- party registry (TASK-019) ----------

    public Map<String, AdventurerParty> getParties() { return parties; }

    public AdventurerParty getParty(String partyId) {
        return parties.get(partyId);
    }

    public void putParty(AdventurerParty party) {
        parties.put(party.getPartyId(), party);
        setDirty();
    }

    public void removeParty(String partyId) {
        if (parties.remove(partyId) != null) {
            setDirty();
        }
    }
}
