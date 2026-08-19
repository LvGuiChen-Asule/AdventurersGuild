package com.adventurersguild.party;

import com.adventurersguild.player.AdventurerData;

/**
 * TASK-002: party reference on the player. Full party data lives in
 * GuildWorldData (world-level), this is just the pointer stored per player.
 */
public class PartyReference {
    private final AdventurerData data;

    public PartyReference(AdventurerData data) {
        this.data = data;
    }

    public String getPartyId() {
        return data.getPartyId();
    }

    public boolean hasParty() {
        String id = data.getPartyId();
        return id != null && !id.isEmpty();
    }

    public void setParty(String partyId) {
        data.setPartyId(partyId);
    }

    public void clear() {
        data.setPartyId(null);
    }
}
