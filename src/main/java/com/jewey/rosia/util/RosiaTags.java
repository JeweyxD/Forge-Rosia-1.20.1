package com.jewey.rosia.util;


import com.jewey.rosia.Rosia;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RosiaTags {
    public static class Blocks {

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(Rosia.MOD_ID, name));
        }
        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }
    public static class Items {
        public static final TagKey<Item> FIRE_BOX_FUEL = tag("fire_box_fuel");
        public static final TagKey<Item> STONE_PATH = tag("stone_path");
        public static final TagKey<Item> CANNED_ITEM = tag("canned_item");
        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(Rosia.MOD_ID, name));
        }
        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }

    }
}

