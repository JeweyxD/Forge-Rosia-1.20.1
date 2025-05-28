package com.jewey.rosia.common.items;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.entities.ModEntities;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.items.*;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Rosia.MOD_ID);


    public static final RegistryObject<Item> COPPER_WIRE = register("copper_wire");

    public static final RegistryObject<Item> COPPER_COIL = register("copper_coil");

    public static final RegistryObject<Item> INVAR_INGOT = register("invar_ingot");

    public static final RegistryObject<Item> INVAR_DOUBLE_INGOT = register("invar_double_ingot");

    public static final RegistryObject<Item> INVAR_SHEET = register("invar_sheet");

    public static final RegistryObject<Item> INVAR_DOUBLE_SHEET = register("invar_double_sheet");

    public static final RegistryObject<Item> INVAR_ROD = register("invar_rod");

    public static final RegistryObject<Item> WEAK_PURPLE_STEEL_INGOT = register("weak_purple_steel_ingot");

    public static final RegistryObject<Item> PURPLE_STEEL_INGOT = register("purple_steel_ingot");

    public static final RegistryObject<Item> PURPLE_STEEL_DOUBLE_INGOT = register("purple_steel_double_ingot");

    public static final RegistryObject<Item> PURPLE_STEEL_SHEET = register("purple_steel_sheet");

    public static final RegistryObject<Item> PURPLE_STEEL_DOUBLE_SHEET = register("purple_steel_double_sheet");

    public static final RegistryObject<Item> PURPLE_STEEL_ROD = register("purple_steel_rod");

    public static final RegistryObject<Item> PURPLE_STEEL_WIRE = register("purple_steel_wire");

    public static final RegistryObject<Item> PURPLE_STEEL_COIL = register("purple_steel_coil");

    public static final RegistryObject<Item> COMPRESSED_MAGNETITE = register("compressed_magnetite");

    public static final RegistryObject<Item> MAGNET = register("magnet");

    public static final RegistryObject<Item> MOTOR = register("motor");

    public static final RegistryObject<Item> STEEL_GRINDSTONE = register("steel_grindstone",
            () -> new Item(properties().durability(1000)));

    public static final RegistryObject<Item> STEEL_MACHINE_DIE = ITEMS.register("steel_machine_die",
            () -> new Item(properties().durability(500)));

    public static final RegistryObject<Item> STEEL_ROLLERS = register("steel_rollers",
            () -> new Item(properties().durability(500)));

    public static final RegistryObject<Item> STEEL_LOOM_PARTS = register("steel_loom_parts",
            () -> new Item(properties().durability(500)));

    public static final RegistryObject<Item> SATCHEL_PART = register("satchel_part");

    public static final RegistryObject<Item> LEATHER_SATCHEL = register("leather_satchel",
            () -> new SatchelItem(properties().stacksTo(1)));

    public static final RegistryObject<Item> UNFINISHED_TOOL_BELT = register("unfinished_tool_belt");

    public static final RegistryObject<Item> TOOL_BELT = register("tool_belt",
            () -> new ToolBeltItem(properties().stacksTo(1)));

    public static final RegistryObject<Item> BULLET = register("bullets");

    public static final RegistryObject<Item> RIFLE_AMMO = register("rifle_ammo",
            () -> new BulletItem(properties()));


    //TOOLS & TOOL HEADS
    public static final RegistryObject<Item> PURPLE_STEEL_RIFLE = register("purple_steel_rifle",
            () -> new RifleItem(properties().durability(2500)));

    public static final RegistryObject<Item> PURPLE_STEEL_TUYERE = register("purple_steel_tuyere",
            () -> new TieredItem(ModTiers.PURPLE_STEEL, properties()));

    public static final RegistryObject<Item> PURPLE_STEEL_FISH_HOOK = register("purple_steel_fish_hook");
    public static final RegistryObject<Item> PURPLE_STEEL_FISHING_ROD = register("purple_steel_fishing_rod",
            () -> new TFCFishingRodItem(properties().durability(7500), ModTiers.PURPLE_STEEL));

    public static final RegistryObject<Item> PURPLE_STEEL_PICKAXE = register("purple_steel_pickaxe",
            () -> new PickaxeItem(ModTiers.PURPLE_STEEL, -3,-2.8F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_PICKAXE_HEAD = register("purple_steel_pickaxe_head");
    public static final RegistryObject<Item> PURPLE_STEEL_PROPICK = register("purple_steel_propick",
            () -> new PropickItem(ModTiers.PURPLE_STEEL, -6,-2.8F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_PROPICK_HEAD = register("purple_steel_propick_head");
    public static final RegistryObject<Item> PURPLE_STEEL_AXE = register("purple_steel_axe",
            () -> new AxeItem(ModTiers.PURPLE_STEEL, 3.5F,-3.1F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_AXE_HEAD = register("purple_steel_axe_head");
    public static final RegistryObject<Item> PURPLE_STEEL_SHOVEL = register("purple_steel_shovel",
            () -> new ShovelItem(ModTiers.PURPLE_STEEL, -2.5F,-3F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_SHOVEL_HEAD = register("purple_steel_shovel_head");
    public static final RegistryObject<Item> PURPLE_STEEL_HOE = register("purple_steel_hoe",
            () -> new TFCHoeItem(ModTiers.PURPLE_STEEL, (int) -2.5,-2F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_HOE_HEAD = register("purple_steel_hoe_head");

    public static final RegistryObject<Item> PURPLE_STEEL_CHISEL = register("purple_steel_chisel",
            () -> new ChiselItem(ModTiers.PURPLE_STEEL, (int) -8.5,-1.5F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_CHISEL_HEAD = register("purple_steel_chisel_head");

    public static final RegistryObject<Item> PURPLE_STEEL_HAMMER = register("purple_steel_hammer",
            () -> new ToolItem(ModTiers.PURPLE_STEEL, -0.5F,-3F,
                    TFCTags.Blocks.MINEABLE_WITH_HAMMER, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_HAMMER_HEAD = register("purple_steel_hammer_head");

    public static final RegistryObject<Item> PURPLE_STEEL_SAW = register("purple_steel_saw",
            () -> new AxeItem(ModTiers.PURPLE_STEEL, -5.5F,-3F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_SAW_BLADE = register("purple_steel_saw_blade");

    public static final RegistryObject<Item> PURPLE_STEEL_JAVELIN = register("purple_steel_javelin",
            () -> new JavelinItem(ModTiers.PURPLE_STEEL, -2, 16, -2.6F, properties(),
                    new ResourceLocation("rosia", "textures/entity/projectiles/purple_steel_javelin_projectile.png")));
    public static final RegistryObject<Item> PURPLE_STEEL_JAVELIN_HEAD = register("purple_steel_javelin_head");

    public static final RegistryObject<Item> PURPLE_STEEL_SWORD = register("purple_steel_sword",
            () -> new SwordItem(ModTiers.PURPLE_STEEL, 0,-2.4F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_SWORD_BLADE = register("purple_steel_sword_blade");

    public static final RegistryObject<Item> PURPLE_STEEL_MACE = register("purple_steel_mace",
            () -> new MaceItem(ModTiers.PURPLE_STEEL, (int) 1F,-3F, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_MACE_BLADE = register("purple_steel_mace_head");

    public static final RegistryObject<Item> PURPLE_STEEL_KNIFE = register("purple_steel_knife",
            () -> new ToolItem(ModTiers.PURPLE_STEEL, -4.5F,-2F,
                    TFCTags.Blocks.MINEABLE_WITH_KNIFE, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_KNIFE_BLADE = register("purple_steel_knife_blade");

    public static final RegistryObject<Item> PURPLE_STEEL_SCYTHE = register("purple_steel_scythe",
            () -> new ScytheItem(ModTiers.PURPLE_STEEL, -4F,-3.2F,
                    TFCTags.Blocks.MINEABLE_WITH_SCYTHE, properties()));
    public static final RegistryObject<Item> PURPLE_STEEL_SCYTHE_BLADE = register("purple_steel_scythe_blade");

    public static final RegistryObject<Item> PURPLE_STEEL_SHEARS = register("purple_steel_shears",
            () -> new ShearsItem(properties().durability(7500)));

    //ARMORS
    public static final RegistryObject<Item> PURPLE_STEEL_UNFINISHED_HELMET = register("purple_steel_unfinished_helmet");

    public static final RegistryObject<Item> PURPLE_STEEL_HELMET = register("purple_steel_helmet",
            () -> new ArmorItem(ModArmorMaterials.PURPLE_STEEL, ArmorItem.Type.HELMET, properties()));

    public static final RegistryObject<Item> PURPLE_STEEL_UNFINISHED_CHESTPLATE = register("purple_steel_unfinished_chestplate");

    public static final RegistryObject<Item> PURPLE_STEEL_CHESTPLATE = register("purple_steel_chestplate",
            () -> new ArmorItem(ModArmorMaterials.PURPLE_STEEL, ArmorItem.Type.CHESTPLATE, properties()));

    public static final RegistryObject<Item> PURPLE_STEEL_UNFINISHED_GREAVES = register("purple_steel_unfinished_greaves");

    public static final RegistryObject<Item> PURPLE_STEEL_GREAVES = register("purple_steel_greaves",
            () -> new ArmorItem(ModArmorMaterials.PURPLE_STEEL, ArmorItem.Type.LEGGINGS, properties()));

    public static final RegistryObject<Item> PURPLE_STEEL_UNFINISHED_BOOTS = register("purple_steel_unfinished_boots");

    public static final RegistryObject<Item> PURPLE_STEEL_BOOTS = register("purple_steel_boots",
            () -> new ArmorItem(ModArmorMaterials.PURPLE_STEEL, ArmorItem.Type.BOOTS, properties()));

    public static final RegistryObject<Item> PURPLE_STEEL_HORSE_ARMOR = register("purple_steel_horse_armor",
            () -> new HorseArmorItem(14, new ResourceLocation("rosia",
                    "textures/entity/animal/horse_armor/purple_steel_horse_armor_entity.png"), properties()));

    public static final RegistryObject<Item> PURPLE_STEEL_SHIELD = register("purple_steel_shield",
            () -> new TFCShieldItem(ModTiers.PURPLE_STEEL, properties()));


    // CANNED FOOD
    public static final Map<Nutrient, RegistryObject<Item>> CANS = Helpers.mapOfKeys(Nutrient.class, nutrient ->
            register("food/" + nutrient.name() + "_can",
                    () -> new CannedFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(4).saturationMod(0.3f).build()))));

    public static final RegistryObject<Item> SOUP_CAN = register("food/soup_can",
            () -> new CannedFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(4).saturationMod(0.3f).build())));

    public static final RegistryObject<Item> TIN_CAN = register("tin_can");


    //DOWN HERE SO IT'S NEAR THE BLOCKS
    public static final RegistryObject<StandingAndWallBlockItem> IRON_SUPPORT = register("iron_support",
            () -> new StandingAndWallBlockItem(ModBlocks.IRON_SUPPORT_VERTICAL.get(), ModBlocks.IRON_SUPPORT_HORIZONTAL.get(), properties(), Direction.UP));

    public static final RegistryObject<Item> LOCOMOTIVE = register("locomotive",
            () -> new LocomotiveItem(new Item.Properties(), ModEntities.LOCOMOTIVE,
                    () -> ModBlocks.STIRLING_ENGINE_SLAB.get().asItem()));


    //Default to Rosia's tab unless otherwise specified
    private static Item.Properties properties()
    {
        return new Item.Properties();
    }

    private static RegistryObject<Item> register(String name) {
        return register(name, () -> new Item(new Item.Properties()));
    }

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item) {
        return ITEMS.register(name.toLowerCase(Locale.ROOT), item);
    }
}
