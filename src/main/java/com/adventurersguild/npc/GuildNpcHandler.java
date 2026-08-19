package com.adventurersguild.npc;

import com.adventurersguild.network.GuildDataSyncPacket;
import com.adventurersguild.network.GuildNetwork;
import com.adventurersguild.dialogue.DialogueManager;
import com.adventurersguild.player.AdventurerCapability;
import com.adventurersguild.quest.QuestManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Guild NPCs (V0.7): vanilla villagers tagged with a role.
 * Receptionist registers/open adventurer info, quest master opens the hall,
 * shopkeeper opens the shop. Core systems never require these NPCs to exist.
 */
public final class GuildNpcHandler {
    public static final String NPC_ROLE_TAG = "ag_npc_role";
    public static final String ROLE_RECEPTIONIST = "receptionist";
    public static final String ROLE_QUEST_MASTER = "questmaster";
    public static final String ROLE_SHOPKEEPER = "shopkeeper";
    public static final String ROLE_EXPEDITION_MASTER = "expedition_master";
    public static final String ROLE_ARCHIVIST = "archivist";
    public static final String ROLE_END_RESEARCHER = "end_researcher";

    private GuildNpcHandler() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getTarget() instanceof AbstractVillager villager) {
            String role = villager.getPersistentData().getString(NPC_ROLE_TAG);
            if (role.isEmpty()) {
                return;
            }
            event.setCanceled(true);
            QuestManager.onNpcInteract(player, role);
            DialogueManager.openDialogue(player, role);
        }
    }

    /** Dev command: spawns a guild NPC near the player. */
    public static boolean spawnNpc(ServerPlayer player, String role) {
        if (!role.equals(ROLE_RECEPTIONIST)
                && !role.equals(ROLE_QUEST_MASTER)
                && !role.equals(ROLE_SHOPKEEPER)
                && !role.equals(ROLE_EXPEDITION_MASTER)
                && !role.equals(ROLE_ARCHIVIST)
                && !role.equals(ROLE_END_RESEARCHER)) {
            player.sendSystemMessage(Component.translatable("msg.adventurersguild.npc_invalid_role", role));
            return false;
        }
        ServerLevel level = player.serverLevel();
        GuildNpcManager.spawnNpcAt(level, player.blockPosition(), role);
        player.sendSystemMessage(Component.translatable("msg.adventurersguild.npc_spawned", roleName(role)));
        return true;
    }

    private static Component roleName(String role) {
        return Component.translatable("npc.adventurersguild." + role).withStyle(ChatFormatting.GOLD);
    }

    public static VillagerProfession profession(String role) {
        return switch (role) {
            case ROLE_RECEPTIONIST -> VillagerProfession.NITWIT;
            case ROLE_QUEST_MASTER -> VillagerProfession.LIBRARIAN;
            case ROLE_SHOPKEEPER -> VillagerProfession.WEAPONSMITH;
            case ROLE_EXPEDITION_MASTER -> VillagerProfession.CARTOGRAPHER;
            case ROLE_ARCHIVIST -> VillagerProfession.CLERIC;
            case ROLE_END_RESEARCHER -> VillagerProfession.SHEPHERD;
            default -> VillagerProfession.NONE;
        };
    }
}
