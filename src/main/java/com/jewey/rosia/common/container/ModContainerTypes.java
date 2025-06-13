package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.*;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ItemStackContainer;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.jewey.rosia.Rosia.MOD_ID;

public class ModContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final RegistryObject<MenuType<MagnetizerContainer>>
            MAGNETIZER = ModContainerTypes.<MagnetizerBlockEntity, MagnetizerContainer>registerBlock(
                    "magnetizer", ModBlockEntities.MAGNETIZER_BLOCK_ENTITY, MagnetizerContainer::create);

    public static final RegistryObject<MenuType<AutoQuernContainer>>
            AUTO_QUERN = ModContainerTypes.<AutoQuernBlockEntity, AutoQuernContainer>registerBlock(
                    "auto_quern", ModBlockEntities.AUTO_QUERN_BLOCK_ENTITY, AutoQuernContainer::create);

    public static final RegistryObject<MenuType<ExtrudingMachineContainer>>
            EXTRUDING_MACHINE = ModContainerTypes.<ExtrudingMachineBlockEntity, ExtrudingMachineContainer>registerBlock(
                    "extruding_machine", ModBlockEntities.EXTRUDING_MACHINE_BLOCK_ENTITY, ExtrudingMachineContainer::create);

    public static final RegistryObject<MenuType<RollingMachineContainer>>
            ROLLING_MACHINE = ModContainerTypes.<RollingMachineBlockEntity, RollingMachineContainer>registerBlock(
                    "rolling_machine", ModBlockEntities.ROLLING_MACHINE_BLOCK_ENTITY, RollingMachineContainer::create);

    public static final RegistryObject<MenuType<CanningPressContainer>>
            CANNING_PRESS = ModContainerTypes.<CanningPressBlockEntity, CanningPressContainer>registerBlock(
            "canning_press", ModBlockEntities.CANNING_PRESS_BLOCK_ENTITY, CanningPressContainer::create);

    public static final RegistryObject<MenuType<ElectricLoomContainer>>
            ELECTRIC_LOOM = ModContainerTypes.<ElectricLoomBlockEntity, ElectricLoomContainer>registerBlock(
            "electric_loom", ModBlockEntities.ELECTRIC_LOOM_BLOCK_ENTITY, ElectricLoomContainer::create);

    public static final RegistryObject<MenuType<ScrapingMachineContainer>>
            SCRAPING_MACHINE = ModContainerTypes.<ScrapingMachineBlockEntity, ScrapingMachineContainer>registerBlock(
            "scraping_machine", ModBlockEntities.SCRAPING_MACHINE_BLOCK_ENTITY, ScrapingMachineContainer::create);

    public static final RegistryObject<MenuType<FireBoxContainer>>
            FIRE_BOX = ModContainerTypes.<FireBoxBlockEntity, FireBoxContainer>registerBlock(
                    "fire_box", ModBlockEntities.FIRE_BOX_BLOCK_ENTITY, FireBoxContainer::create);

    public static final RegistryObject<MenuType<MechanicalGeneratorContainer>>
            MECHANICAL_GENERATOR = ModContainerTypes.<MechanicalGeneratorBlockEntity, MechanicalGeneratorContainer>registerBlock(
                    "mechanical_generator", ModBlockEntities.MECHANICAL_GENERATOR_BLOCK_ENTITY, MechanicalGeneratorContainer::create);

    public static final RegistryObject<MenuType<SteamGeneratorContainer>>
            STEAM_GENERATOR = ModContainerTypes.<SteamGeneratorBlockEntity, SteamGeneratorContainer>registerBlock(
                    "steam_generator", ModBlockEntities.STEAM_GENERATOR_BLOCK_ENTITY, SteamGeneratorContainer::create);

    public static final RegistryObject<MenuType<WaterPumpContainer>>
            WATER_PUMP = ModContainerTypes.<WaterPumpBlockEntity, WaterPumpContainer>registerBlock(
                    "pump", ModBlockEntities.WATER_PUMP_BLOCK_ENTITY, WaterPumpContainer::create);

    public static final RegistryObject<MenuType<ElectricForgeContainer>>
            ELECTRIC_FORGE = ModContainerTypes.<ElectricForgeBlockEntity, ElectricForgeContainer>registerBlock(
                    "electric_forge", ModBlockEntities.ELECTRIC_FORGE_BLOCK_ENTITY, ElectricForgeContainer::create);

    public static final RegistryObject<MenuType<NickelIronBatteryContainer>>
            NICKEL_IRON_BATTERY = ModContainerTypes.<NickelIronBatteryBlockEntity, NickelIronBatteryContainer>registerBlock(
                    "nickel_iron_battery", ModBlockEntities.NICKEL_IRON_BATTERY_BLOCK_ENTITY, NickelIronBatteryContainer::create);

    public static final RegistryObject<MenuType<ZincSilverBatteryContainer>>
            ZINC_SILVER_BATTERY = ModContainerTypes.<ZincSilverBatteryBlockEntity, ZincSilverBatteryContainer>registerBlock(
                    "zinc_silver_battery", ModBlockEntities.ZINC_SILVER_BATTERY_BLOCK_ENTITY, ZincSilverBatteryContainer::create);

    public static final RegistryObject<MenuType<ElectricGrillContainer>>
            ELECTRIC_GRILL = ModContainerTypes.<ElectricGrillBlockEntity, ElectricGrillContainer>registerBlock(
            "electric_grill", ModBlockEntities.ELECTRIC_GRILL_BLOCK_ENTITY, ElectricGrillContainer::create);

    public static final RegistryObject<MenuType<FridgeContainer>>
            FRIDGE = ModContainerTypes.<FridgeBlockEntity, FridgeContainer>registerBlock(
            "fridge", ModBlockEntities.FRIDGE_BLOCK_ENTITY, FridgeContainer::create);

    public static final RegistryObject<MenuType<CharcoalKilnContainer>>
            CHARCOAL_KILN = ModContainerTypes.<CharcoalKilnBlockEntity, CharcoalKilnContainer>registerBlock(
            "charcoal_kiln", ModBlockEntities.CHARCOAL_KILN_BLOCK_ENTITY, CharcoalKilnContainer::create);

    public static final RegistryObject<MenuType<BoilingCauldronContainer>>
            BOILING_CAULDRON = ModContainerTypes.<BoilingCauldronBlockEntity, BoilingCauldronContainer>registerBlock(
            "boiling_cauldron", ModBlockEntities.BOILING_CAULDRON_BLOCK_ENTITY, BoilingCauldronContainer::create);

    public static final RegistryObject<MenuType<LavaBasinContainer>>
            LAVA_BASIN = ModContainerTypes.<LavaBasinBlockEntity, LavaBasinContainer>registerBlock(
            "lava_basin", ModBlockEntities.LAVA_BASIN_BLOCK_ENTITY, LavaBasinContainer::create);

    public static final RegistryObject<MenuType<LeatherSatchelContainer>>
            LEATHER_SATCHEL = registerItem("leather_satchel", LeatherSatchelContainer::create);

    public static final RegistryObject<MenuType<ToolBeltContainer>>
            TOOL_BELT = registerItem("tool_belt", ToolBeltContainer::create);

    public static final RegistryObject<MenuType<ProspectingKitContainer>>
            PROSPECTING_KIT = registerItem("prospecting_kit", ProspectingKitContainer::create);

    public static final RegistryObject<MenuType<LocomotiveContainer>>
            LOCOMOTIVE = ModContainerTypes.<LocomotiveContainer>register(
                    "locomotive", LocomotiveContainer::createMenu);

    private static <T extends InventoryBlockEntity<?>, C extends BlockEntityContainer<T>> RegistryObject<MenuType<C>> registerBlock(String name, Supplier<BlockEntityType<T>> type, BlockEntityContainer.Factory<T, C> factory)
    {
        return RegistrationHelpers.registerBlockEntityContainer(CONTAINERS, name, type, factory);
    }

    private static <C extends ItemStackContainer> RegistryObject<MenuType<C>> registerItem(String name, ItemStackContainer.Factory<C> factory)
    {
        return RegistrationHelpers.registerItemStackContainer(CONTAINERS, name, factory);
    }

    private static <C extends AbstractContainerMenu> RegistryObject<MenuType<C>> register(String name, IContainerFactory<C> factory)
    {
        return RegistrationHelpers.registerContainer(CONTAINERS, name, factory);
    }
}


