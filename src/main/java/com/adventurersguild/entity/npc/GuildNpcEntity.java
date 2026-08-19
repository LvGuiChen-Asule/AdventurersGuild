package com.adventurersguild.entity.npc;

import com.adventurersguild.npc.GuildNpcHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * TASK-006/007: server-authoritative guild NPC entity.
 * - NPC id / role / name / dialogue group (role) / home position stored in NBT
 * - Persistence required (survives restarts, no duplicate spawns)
 * - Vanilla villager brain is intentionally skipped; behavior is driven by
 *   the lightweight schedule/wander/return-home goals below.
 */
public class GuildNpcEntity extends Villager {
    public static final String TAG_ROLE = "ag_npc_role";
    public static final String TAG_HOME = "ag_npc_home";

    private String role;
    private BlockPos home;

    public GuildNpcEntity(EntityType<? extends GuildNpcEntity> type, Level level, String role) {
        super(type, level);
        this.role = role;
        this.setPersistenceRequired();
        this.setAge(0);
        applyRoleData();
    }

    public String getRole() {
        if (role == null || role.isEmpty()) {
            role = getPersistentData().getString(TAG_ROLE);
        }
        return role;
    }

    public BlockPos getHomePosition() {
        if (home == null) {
            int[] stored = getPersistentData().getIntArray(TAG_HOME);
            home = stored.length == 3 ? new BlockPos(stored[0], stored[1], stored[2]) : blockPosition();
        }
        return home;
    }

    public void setHomePosition(BlockPos pos) {
        this.home = pos;
        getPersistentData().putIntArray(TAG_HOME, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    public void applyRoleData() {
        String r = getRole();
        VillagerProfession profession = GuildNpcHandler.profession(r);
        this.setVillagerData(new VillagerData(VillagerType.PLAINS, profession, 1));
        getPersistentData().putString(TAG_ROLE, r);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Monster.class, 10.0f, 0.5, 0.6));
        this.goalSelector.addGoal(2, new GuildScheduleGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
    }

    @Override
    protected void customServerAiStep() {
        // Skip the vanilla villager brain: schedule & behavior come from our goals.
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        String r = getRole();
        if (!r.isEmpty()) {
            tag.putString(TAG_ROLE, r);
        }
        if (home != null) {
            tag.putIntArray(TAG_HOME, new int[]{home.getX(), home.getY(), home.getZ()});
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.role = tag.getString(TAG_ROLE);
        int[] stored = tag.getIntArray(TAG_HOME);
        if (stored.length == 3) {
            this.home = new BlockPos(stored[0], stored[1], stored[2]);
            getPersistentData().putIntArray(TAG_HOME, stored);
        }
        getPersistentData().putString(TAG_ROLE, getRole());
        applyRoleData();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createAttributes();
    }

    /**
     * Lightweight schedule goal:
     * 06:00-08:00 idle, 08:00-12:00 work (stand at home), 12:00-13:00 rest,
     * 13:00-18:00 work, 18:00-22:00 free wander, 22:00-06:00 return home.
     * Returns home if too far away at any time.
     */
    private static class GuildScheduleGoal extends Goal {
        private final GuildNpcEntity npc;
        private int wanderCooldown;

        GuildScheduleGoal(GuildNpcEntity npc) {
            this.npc = npc;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }

        @Override
        public void tick() {
            long dayTime = npc.level().getDayTime() % 24000L;
            boolean night = dayTime >= 16000L || dayTime < 2000L;
            boolean work = (dayTime >= 2000L && dayTime < 6000L)
                    || (dayTime >= 7000L && dayTime < 12000L);
            boolean free = dayTime >= 12000L && dayTime < 16000L;

            BlockPos home = npc.getHomePosition();
            double distSq = npc.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);

            if (night || distSq > 24.0 * 24.0) {
                npc.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.6);
            } else if (free) {
                if (--wanderCooldown <= 0) {
                    wanderCooldown = 80;
                    int dx = npc.getRandom().nextInt(25) - 12;
                    int dz = npc.getRandom().nextInt(25) - 12;
                    npc.getNavigation().moveTo(
                            home.getX() + 0.5 + dx, home.getY(), home.getZ() + 0.5 + dz, 0.5);
                }
            } else if (work) {
                npc.getNavigation().stop();
            }
        }
    }
}
