package com.adventurersguild.player;

import com.adventurersguild.AdventurersGuild;
import com.adventurersguild.party.PartyReference;
import com.adventurersguild.quest.QuestState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Attaches the server-authoritative {@link AdventurerData} to every player and
 * keeps it persisted by Minecraft's player save system.
 */
public final class AdventurerCapability {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AdventurersGuild.MOD_ID, "adventurer");
    public static final Capability<AdventurerData> ADVENTURER = CapabilityManager.get(new CapabilityToken<>() {});

    private AdventurerCapability() {}

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ID, new Provider());
        }
    }

    /** Preserve guild data across death and dimension/logout-related entity cloning. */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        oldPlayer.getCapability(ADVENTURER).ifPresent(old -> {
            newPlayer.getCapability(ADVENTURER).ifPresent(newData -> newData.copyFrom(old));
        });
        oldPlayer.reviveCaps();
    }

    public static AdventurerData get(Player player) {
        return player.getCapability(ADVENTURER).orElse(null);
    }

    public static boolean isRegistered(Player player) {
        AdventurerData data = get(player);
        return data != null && data.isRegistered();
    }

    public static AdventurerProfile getProfile(Player player) {
        AdventurerData data = get(player);
        return data == null ? null : new AdventurerProfile(data);
    }

    public static QuestState getQuestState(Player player) {
        AdventurerData data = get(player);
        return data == null ? null : new QuestState(data);
    }

    public static ChronicleState getChronicleState(Player player) {
        AdventurerData data = get(player);
        return data == null ? null : new ChronicleState(data);
    }

    public static UnlockState getUnlockState(Player player) {
        AdventurerData data = get(player);
        return data == null ? null : new UnlockState(data);
    }

    public static PartyReference getPartyReference(Player player) {
        AdventurerData data = get(player);
        return data == null ? null : new PartyReference(data);
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final AdventurerData data = new AdventurerData();
        private final LazyOptional<AdventurerData> lazy = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return capability == ADVENTURER ? lazy.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.save();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.load(nbt);
        }
    }
}
