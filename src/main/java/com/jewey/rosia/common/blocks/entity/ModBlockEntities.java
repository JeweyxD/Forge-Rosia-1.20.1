package com.jewey.rosia.common.blocks.entity;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.blocks.entity.block_entity.*;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Rosia.MOD_ID);

    public static final RegistryObject<BlockEntityType<MagnetizerBlockEntity>> MAGNETIZER_BLOCK_ENTITY =
            register("magnetizer_block_entity", MagnetizerBlockEntity::new, ModBlocks.MAGNETIZER);

    public static final RegistryObject<BlockEntityType<AutoQuernBlockEntity>> AUTO_QUERN_BLOCK_ENTITY =
            register("auto_quern_block_entity", AutoQuernBlockEntity::new, ModBlocks.AUTO_QUERN);

    public static final RegistryObject<BlockEntityType<FireBoxBlockEntity>> FIRE_BOX_BLOCK_ENTITY =
            register("fire_box_block_entity", FireBoxBlockEntity::new, ModBlocks.FIRE_BOX);

    public static final RegistryObject<BlockEntityType<MechanicalGeneratorBlockEntity>> MECHANICAL_GENERATOR_BLOCK_ENTITY =
            register("mechanical_generator_block_entity", MechanicalGeneratorBlockEntity::new, ModBlocks.MECHANICAL_GENERATOR);

    public static final RegistryObject<BlockEntityType<SteamGeneratorBlockEntity>> STEAM_GENERATOR_BLOCK_ENTITY =
            register("steam_generator_block_entity", SteamGeneratorBlockEntity::new, ModBlocks.STEAM_GENERATOR);

    public static final RegistryObject<BlockEntityType<NickelIronBatteryBlockEntity>> NICKEL_IRON_BATTERY_BLOCK_ENTITY =
            register("nickel_iron_battery_block_entity", NickelIronBatteryBlockEntity::new, ModBlocks.NICKEL_IRON_BATTERY);

    public static final RegistryObject<BlockEntityType<ZincSilverBatteryBlockEntity>> ZINC_SILVER_BATTERY_BLOCK_ENTITY =
            register("zinc_silver_battery_block_entity", ZincSilverBatteryBlockEntity::new, ModBlocks.ZINC_SILVER_BATTERY);

    public static final RegistryObject<BlockEntityType<PowerConduitBlockEntity>> POWER_CONDUIT_BLOCK_ENTITY =
            register("power_conduit_block_entity", PowerConduitBlockEntity::new, Stream.of(ModBlocks.POWER_CONDUIT, ModBlocks.ENCASED_POWER_CONDUIT));

    public static final RegistryObject<BlockEntityType<WaterPumpBlockEntity>> WATER_PUMP_BLOCK_ENTITY =
            register("water_pump_block_entity", WaterPumpBlockEntity::new, ModBlocks.WATER_PUMP);

    public static final RegistryObject<BlockEntityType<PressurizedPipeBlockEntity>> PRESSURIZED_PIPE_BLOCK_ENTITY =
            register("pressurized_pipe_block_entity", PressurizedPipeBlockEntity::new, Stream.of(ModBlocks.PRESSURIZED_PIPE, ModBlocks.ENCASED_PRESSURIZED_PIPE));

    public static final RegistryObject<BlockEntityType<ExtrudingMachineBlockEntity>> EXTRUDING_MACHINE_BLOCK_ENTITY =
            register("extruding_machine_block_entity", ExtrudingMachineBlockEntity::new, ModBlocks.EXTRUDING_MACHINE);

    public static final RegistryObject<BlockEntityType<RollingMachineBlockEntity>> ROLLING_MACHINE_BLOCK_ENTITY =
            register("rolling_machine_block_entity", RollingMachineBlockEntity::new, ModBlocks.ROLLING_MACHINE);

    public static final RegistryObject<BlockEntityType<ElectricForgeBlockEntity>> ELECTRIC_FORGE_BLOCK_ENTITY =
            register("electric_forge_block_entity", ElectricForgeBlockEntity::new, ModBlocks.ELECTRIC_FORGE);

    public static final RegistryObject<BlockEntityType<ElectricGrillBlockEntity>> ELECTRIC_GRILL_BLOCK_ENTITY =
            register("electric_grill_block_entity", ElectricGrillBlockEntity::new, ModBlocks.ELECTRIC_GRILL);

    public static final RegistryObject<BlockEntityType<FridgeBlockEntity>> FRIDGE_BLOCK_ENTITY =
            register("fridge_block_entity", FridgeBlockEntity::new, ModBlocks.FRIDGE);

    public static final RegistryObject<BlockEntityType<ElectricLanternBlockEntity>> ELECTRIC_LANTERN_BLOCK_ENTITY =
            register("electric_lantern_block_entity", ElectricLanternBlockEntity::new, ModBlocks.ELECTRIC_LANTERN);

    public static final RegistryObject<BlockEntityType<CharcoalKilnBlockEntity>> CHARCOAL_KILN_BLOCK_ENTITY =
            register("charcoal_kiln_block_entity", CharcoalKilnBlockEntity::new, ModBlocks.CHARCOAL_KILN);

    public static final RegistryObject<BlockEntityType<CanningPressBlockEntity>> CANNING_PRESS_BLOCK_ENTITY =
            register("canning_press_block_entity", CanningPressBlockEntity::new, ModBlocks.CANNING_PRESS);

    public static final RegistryObject<BlockEntityType<ElectricLoomBlockEntity>> ELECTRIC_LOOM_BLOCK_ENTITY =
            register("electric_loom_block_entity", ElectricLoomBlockEntity::new, ModBlocks.ELECTRIC_LOOM);

    public static final RegistryObject<BlockEntityType<ScrapingMachineBlockEntity>> SCRAPING_MACHINE_BLOCK_ENTITY =
            register("scraping_machine_block_entity", ScrapingMachineBlockEntity::new, ModBlocks.SCRAPING_MACHINE);

    public static final RegistryObject<BlockEntityType<BoilingCauldronBlockEntity>> BOILING_CAULDRON_BLOCK_ENTITY =
            register("boiling_cauldron_block_entity", BoilingCauldronBlockEntity::new, ModBlocks.BOILING_CAULDRON);

    public static final RegistryObject<BlockEntityType<LavaBasinBlockEntity>> LAVA_BASIN_BLOCK_ENTITY =
            register("lava_basin_block_entity", LavaBasinBlockEntity::new, ModBlocks.LAVA_BASIN);


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block)
    {
        return RegistrationHelpers.register(BLOCK_ENTITY_TYPES, name, factory, block);
    }

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Stream<? extends Supplier<? extends Block>> blocks)
    {
        return RegistrationHelpers.register(BLOCK_ENTITY_TYPES, name, factory, blocks);
    }

}
