package com.jewey.rosia.common.items;

import com.jewey.rosia.Rosia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Rosia.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ROSIA_TAB = CREATIVE_MODE_TABS.register("rosia_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.COPPER_COIL.get()))
                    .title(Component.translatable("creativetab.rosia_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        for(RegistryObject<Item>item : ModItems.ITEMS.getEntries()) {
                            pOutput.accept(item.get());
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
