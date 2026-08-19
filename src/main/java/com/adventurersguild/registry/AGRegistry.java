package com.adventurersguild.registry;

import com.adventurersguild.AdventurersGuild;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

/** Item / block / creative tab registrations (V0.6 equipment + V0.7 guild terminal). */
public final class AGRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AdventurersGuild.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AdventurersGuild.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdventurersGuild.MOD_ID);

    // Guild terminal block (V0.7)
    public static final RegistryObject<Block> GUILD_TERMINAL = BLOCKS.register("guild_terminal",
            () -> new GuildTerminalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> GUILD_TERMINAL_ITEM = ITEMS.register("guild_terminal",
            () -> new BlockItem(GUILD_TERMINAL.get(), new Item.Properties()));

    // Accessories (V0.6)
    public static final RegistryObject<Item> ADVENTURER_BADGE = ITEMS.register("adventurer_badge", AGRegistry::accessory);
    public static final RegistryObject<Item> MINER_RING = ITEMS.register("miner_ring", AGRegistry::accessory);
    public static final RegistryObject<Item> HUNTER_BADGE = ITEMS.register("hunter_badge", AGRegistry::accessory);
    public static final RegistryObject<Item> EXPLORER_CHARM = ITEMS.register("explorer_charm", AGRegistry::accessory);
    public static final RegistryObject<Item> GUILD_BADGE = ITEMS.register("guild_badge", AGRegistry::accessory);

    public static final List<RegistryObject<Item>> ACCESSORIES = List.of(
            ADVENTURER_BADGE, MINER_RING, HUNTER_BADGE, EXPLORER_CHARM, GUILD_BADGE
    );

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.adventurersguild"))
                    .icon(() -> new ItemStack(GUILD_BADGE.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(GUILD_TERMINAL_ITEM.get()));
                        for (RegistryObject<Item> item : ACCESSORIES) {
                            output.accept(new ItemStack(item.get()));
                        }
                    })
                    .build());

    private AGRegistry() {}

    private static Item accessory() {
        return new Item(new Item.Properties().stacksTo(1));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
