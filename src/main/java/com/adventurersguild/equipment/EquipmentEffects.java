package com.adventurersguild.equipment;

import com.adventurersguild.data.EquipmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Applies accessory effects. Curios integration is a soft dependency:
 * when Curios is present, equipped curios are read via reflection; otherwise
 * main-hand / off-hand stacks are used as a demo fallback so the core loop
 * always works without Curios.
 */
public final class EquipmentEffects {
    public static final String EFFECT_QUEST_EXP = "quest_exp";
    public static final String EFFECT_GOLD_REWARD = "gold_reward";
    public static final String EFFECT_MINING_SPEED = "mining_speed";
    public static final String EFFECT_HOSTILE_DAMAGE = "hostile_damage";
    public static final String EFFECT_MOVE_SPEED = "move_speed";

    private static final UUID MOVE_SPEED_MODIFIER_ID =
            UUID.fromString("2c1f1c8e-2f0a-4b6f-9a2e-1b5d3a6f8c9d");
    private static final String CURIO_CAP_CLASS = "top.theillusivec4.curios.api.CuriosCapability";

    private EquipmentEffects() {}

    /** Sums the value of all equipped accessories with the given effect. */
    public static double getEffectSum(Player player, String effect) {
        double sum = 0.0;
        for (ItemStack stack : getAccessoryStacks(player)) {
            EquipmentData data = EquipmentRegistry.getByItem(stack.getItem());
            if (data != null && data.getEffect().equals(effect)) {
                sum += data.getValue();
            }
        }
        return sum;
    }

    /** Applies/removes the movement speed modifier based on equipped explorer charms. */
    public static void applyMoveSpeedModifier(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(MOVE_SPEED_MODIFIER_ID);
        double bonus = getEffectSum(player, EFFECT_MOVE_SPEED);
        if (bonus > 0) {
            attribute.addTransientModifier(new AttributeModifier(
                    MOVE_SPEED_MODIFIER_ID, "ag_explorer_charm",
                    bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> getAccessoryStacks(Player player) {
        List<ItemStack> result = new ArrayList<>();
        // Curios (soft dependency, via reflection)
        try {
            Class<?> capClass = Class.forName(CURIO_CAP_CLASS);
            Capability<Object> capability = (Capability<Object>) capClass.getField("ITEM").get(null);
            player.getCapability(capability).ifPresent(handler -> {
                try {
                    Object curios = handler.getClass().getMethod("getCurios").invoke(handler);
                    if (curios instanceof Map<?, ?> map) {
                        for (Object slotHandler : map.values()) {
                            Object stacksGroup = slotHandler.getClass().getMethod("getStacks").invoke(slotHandler);
                            Object slots = stacksGroup.getClass().getMethod("getSlots").invoke(stacksGroup);
                            if (slots instanceof Iterable<?> iterable) {
                                for (Object slot : iterable) {
                                    Object stack = slot.getClass().getMethod("getStack").invoke(slot);
                                    if (stack instanceof ItemStack itemStack && !itemStack.isEmpty()) {
                                        result.add(itemStack);
                                    }
                                }
                            }
                        }
                    }
                } catch (ReflectiveOperationException ignored) {
                    // Curios API internals changed; fall through to demo mode.
                }
            });
        } catch (ReflectiveOperationException ignored) {
            // Curios not installed.
        }
        // Demo fallback: held items count as "equipped" without Curios.
        if (!player.getMainHandItem().isEmpty()) {
            result.add(player.getMainHandItem());
        }
        if (!player.getOffhandItem().isEmpty()) {
            result.add(player.getOffhandItem());
        }
        return result;
    }
}
