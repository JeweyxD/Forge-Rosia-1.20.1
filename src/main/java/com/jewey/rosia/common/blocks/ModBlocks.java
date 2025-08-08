package com.jewey.rosia.common.blocks;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.block.*;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.*;
import com.jewey.rosia.common.fluids.ModFluids;
import com.jewey.rosia.common.items.DoubleTallBlockItem;
import com.jewey.rosia.common.items.DoubleWideBlockItem;
import com.jewey.rosia.common.items.ModItems;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.HorizontalSupportBlock;
import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Rosia.MOD_ID);

    public static final Supplier<? extends Block> STIRLING_ENGINE_SLAB = registerBlock("stirling_engine_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final Supplier<? extends Block> FIRE_BOX = registerBlock("fire_box",
            () -> new fire_box(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(fire_box.HEAT) * 2)
                    .pathType(BlockPathTypes.DAMAGE_FIRE).blockEntity(ModBlockEntities.FIRE_BOX_BLOCK_ENTITY)
                    .serverTicks(FireBoxBlockEntity::serverTick)));

    public static final RegistryObject<Block> MACHINE_FRAME = registerBlock("machine_frame",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final Supplier<? extends Block> MECHANICAL_GENERATOR = registerBlock("mechanical_generator",
            () -> new mechanical_generator(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL)
                    .blockEntity(ModBlockEntities.MECHANICAL_GENERATOR_BLOCK_ENTITY)
                    .serverTicks(MechanicalGeneratorBlockEntity::serverTick)));

    public static final Supplier<? extends Block> STEAM_GENERATOR = registerBlock("steam_generator",
            () -> new steam_generator(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).pathType(BlockPathTypes.DAMAGE_FIRE)
                    .blockEntity(ModBlockEntities.STEAM_GENERATOR_BLOCK_ENTITY)
                    .serverTicks(SteamGeneratorBlockEntity::serverTick)));

    public static final Supplier<? extends Block> NICKEL_IRON_BATTERY = registerBlock("nickel_iron_battery",
            () -> new nickel_iron_battery(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.NICKEL_IRON_BATTERY_BLOCK_ENTITY)
                    .serverTicks(NickelIronBatteryBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> ZINC_SILVER_BATTERY = registerBlock("zinc_silver_battery",
            () -> new zinc_silver_battery(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.ZINC_SILVER_BATTERY_BLOCK_ENTITY)
                    .serverTicks(ZincSilverBatteryBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> POWER_CONDUIT = registerBlock("power_conduit",
            () -> new power_conduit(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY)
                    .serverTicks(PowerConduitBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> ENCASED_POWER_CONDUIT = registerBlock("encased_power_conduit",
            () -> new power_conduit(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY)
                    .serverTicks(PowerConduitBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> WATER_PUMP = registerBlock("water_pump",
            () -> new water_pump(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.WATER_PUMP_BLOCK_ENTITY)
                    .serverTicks(WaterPumpBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> PRESSURIZED_PIPE = registerBlock("pressurized_pipe",
            () -> new pressurized_pipe(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY)
                    .serverTicks(PressurizedPipeBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> ENCASED_PRESSURIZED_PIPE = registerBlock("encased_pressurized_pipe",
            () -> new pressurized_pipe(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY)
                    .serverTicks(PressurizedPipeBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> MAGNETIZER = registerBlock("magnetizer",
            () -> new magnetizer(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.MAGNETIZER_BLOCK_ENTITY)
                    .serverTicks(MagnetizerBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> AUTO_QUERN = registerBlock("auto_quern",
            () -> new auto_quern(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.AUTO_QUERN_BLOCK_ENTITY)
                    .serverTicks(AutoQuernBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> EXTRUDING_MACHINE = registerBlock("extruding_machine",
            () -> new extruding_machine(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.EXTRUDING_MACHINE_BLOCK_ENTITY)
                    .serverTicks(ExtrudingMachineBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> ROLLING_MACHINE = registerBlock("rolling_machine",
            () -> new rolling_machine(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.ROLLING_MACHINE_BLOCK_ENTITY)
                    .serverTicks(RollingMachineBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> ELECTRIC_LOOM = registerBlock("electric_loom",
            () -> new electric_loom(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.ELECTRIC_LOOM_BLOCK_ENTITY)
                    .serverTicks(ElectricLoomBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> SCRAPING_MACHINE = registerBlock("scraping_machine",
            () -> new scraping_machine(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.SCRAPING_MACHINE_BLOCK_ENTITY)
                    .serverTicks(ScrapingMachineBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> LAVA_BASIN = registerBlock("lava_basin",
            () -> new lava_basin(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(lava_basin.LIT) ? 15 : 0)
                    .pathType(BlockPathTypes.DAMAGE_FIRE).blockEntity(ModBlockEntities.LAVA_BASIN_BLOCK_ENTITY)
                    .serverTicks(LavaBasinBlockEntity::serverTick)));

    public static final Supplier<? extends Block> ELECTRIC_FORGE = registerBlock("electric_forge",
            () -> new electric_forge(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(electric_forge.HEAT) * 2)
                    .pathType(BlockPathTypes.DAMAGE_FIRE).blockEntity(ModBlockEntities.ELECTRIC_FORGE_BLOCK_ENTITY)
                    .serverTicks(ElectricForgeBlockEntity::serverTick)));

    public static final Supplier<? extends Block> BOILING_CAULDRON = registerBlock("boiling_cauldron",
            () -> new boiling_cauldron(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).pathType(BlockPathTypes.DAMAGE_FIRE)
                    .blockEntity(ModBlockEntities.BOILING_CAULDRON_BLOCK_ENTITY)
                    .serverTicks(BoilingCauldronBlockEntity::serverTick)));

    public static final Supplier<? extends Block> CANNING_PRESS = registerBlock("canning_press",
            () -> new canning_press(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.CANNING_PRESS_BLOCK_ENTITY)
                    .serverTicks(CanningPressBlockEntity::serverTick).noOcclusion()));

    public static final Supplier<? extends Block> ELECTRIC_GRILL = registerDoubleWideBlock("electric_grill",
            () -> new electric_grill(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(electric_grill.ON) ? 12 : 0)
                    .pathType(BlockPathTypes.DAMAGE_FIRE).blockEntity(ModBlockEntities.ELECTRIC_GRILL_BLOCK_ENTITY)
                    .serverTicks(ElectricGrillBlockEntity::serverTick)));

    public static final Supplier<? extends Block> FRIDGE = registerDoubleTallBlock("fridge",
            () -> new fridge(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.FRIDGE_BLOCK_ENTITY)
                    .serverTicks(FridgeBlockEntity::serverTick)));

    public static final Supplier<? extends Block> ELECTRIC_LANTERN = registerBlock("electric_lantern",
            () -> new electric_lantern(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(electric_lantern.ON) ? 15 : 0)
                    .blockEntity(ModBlockEntities.ELECTRIC_LANTERN_BLOCK_ENTITY)
                    .serverTicks(ElectricLanternBlockEntity::serverTick)));

    public static final Supplier<? extends Block> CHARCOAL_KILN = registerBlock("charcoal_kiln",
            () -> new charcoal_kiln(ExtendedProperties.of(MapColor.STONE).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.STONE).lightLevel((state) -> state.getValue(charcoal_kiln.LIT) ? 8 : 0)
                    .blockEntity(ModBlockEntities.CHARCOAL_KILN_BLOCK_ENTITY)
                    .serverTicks(CharcoalKilnBlockEntity::serverTick)));

    public static final Supplier<? extends Block> COOLING_BASIN = registerBlock("cooling_basin",
            () -> new cooling_basin(ExtendedProperties.of(MapColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.COOLING_BASIN_BLOCK_ENTITY)
                    .serverTicks(CoolingBasinBlockEntity::serverTick).noOcclusion()));



    // DON'T MAKE ITEMS FOR THE SUPPORT BEAMS IT SCREWS UP EVERYTHING!!!
    public static final Supplier<? extends Block> IRON_SUPPORT_VERTICAL = registerBlockNoItem("iron_support_vertical",
            () -> new VerticalSupportBlock(ExtendedProperties.of(MapColor.METAL).strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final Supplier<? extends Block> IRON_SUPPORT_HORIZONTAL = registerBlockNoItem("iron_support_horizontal",
            () -> new HorizontalSupportBlock(ExtendedProperties.of(MapColor.METAL).strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));


    // PATHS
    public static final RegistryObject<Block> ANDESITE_PATH = registerBlock("andesite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f,10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> BASALT_PATH = registerBlock("basalt_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f,10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> CHALK_PATH = registerBlock("chalk_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> CHERT_PATH = registerBlock("chert_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> CLAYSTONE_PATH = registerBlock("claystone_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> CONGLOMERATE_PATH = registerBlock("conglomerate_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> DACITE_PATH = registerBlock("dacite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> DIORITE_PATH = registerBlock("diorite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> DOLOMITE_PATH = registerBlock("dolomite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> GABBRO_PATH = registerBlock("gabbro_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> GNEISS_PATH = registerBlock("gneiss_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> GRANITE_PATH = registerBlock("granite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> LIMESTONE_PATH = registerBlock("limestone_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> MARBLE_PATH = registerBlock("marble_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> PHYLLITE_PATH = registerBlock("phyllite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> QUARTZITE_PATH = registerBlock("quartzite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> RHYOLITE_PATH = registerBlock("rhyolite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> SCHIST_PATH = registerBlock("schist_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> SHALE_PATH = registerBlock("shale_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> SLATE_PATH = registerBlock("slate_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));


    //Fluids
    public static final RegistryObject<LiquidBlock> INVAR_FLUID = registerBlockNoItem("invar_fluid",
            () -> new LiquidBlock(ModFluids.INVAR_FLUID.flowing(), BlockBehaviour.Properties.copy(Blocks.LAVA).noLootTable()));

    public static final RegistryObject<LiquidBlock> WEAK_PURPLE_STEEL_FLUID = registerBlockNoItem("weak_purple_steel_fluid",
            () -> new LiquidBlock(ModFluids.WEAK_PURPLE_STEEL_FLUID.flowing(), BlockBehaviour.Properties.copy(Blocks.LAVA).noLootTable()));

    public static final RegistryObject<LiquidBlock> PURPLE_STEEL_FLUID = registerBlockNoItem("purple_steel_fluid",
            () -> new LiquidBlock(ModFluids.PURPLE_STEEL_FLUID.flowing(), BlockBehaviour.Properties.copy(Blocks.LAVA).noLootTable()));






    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
    {
        return RegistrationHelpers.registerBlock(ModBlocks.BLOCKS, ModItems.ITEMS, name, blockSupplier, blockItemFactory);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> RegistryObject<T> registerDoubleTallBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerDoubleTallBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> RegistryObject<T> registerDoubleWideBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerDoubleWideBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> RegistryObject<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
    }
    private static <T extends Block> RegistryObject<Item> registerDoubleTallBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new DoubleTallBlockItem(block.get(),
                new Item.Properties()));
    }
    private static <T extends Block> RegistryObject<Item> registerDoubleWideBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new DoubleWideBlockItem(block.get(),
                new Item.Properties()));
    }
}
