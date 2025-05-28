package com.jewey.rosia.util;

import com.jewey.rosia.common.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;

import static net.minecraft.client.renderer.item.ItemProperties.register;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        makeRifle(ModItems.PURPLE_STEEL_RIFLE.get());
    }

    private static void makeRifle(Item item) {
        register(item, new ResourceLocation("pull"), (itemStack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(itemStack) ? 0.0F : (float)(itemStack.getUseDuration() -
                        livingEntity.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(itemStack);
            }
        });
        register(item, new ResourceLocation("pulling"), (itemStack, clientLevel, livingEntity, i) -> {
            return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() ==
                    itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
        });
        register(item, new ResourceLocation("charged"), (itemStack, clientLevel, livingEntity, i) -> {
            return livingEntity != null && CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
        });
    }
}
