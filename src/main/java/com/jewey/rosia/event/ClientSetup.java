package com.jewey.rosia.event;

import com.jewey.rosia.common.container.ModContainerTypes;
import com.jewey.rosia.screen.*;
import com.jewey.rosia.util.ModItemProperties;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientSetup {
    public static void register() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //SCREEN REGISTER
            //BLOCK INVENTORY
        MenuScreens.register(ModContainerTypes.AUTO_QUERN.get(), AutoQuernScreen::new);
        MenuScreens.register(ModContainerTypes.FIRE_BOX.get(), FireBoxScreen::new);
        MenuScreens.register(ModContainerTypes.MECHANICAL_GENERATOR.get(), MechanicalGeneratorScreen::new);
        MenuScreens.register(ModContainerTypes.STEAM_GENERATOR.get(), SteamGeneratorScreen::new);
        MenuScreens.register(ModContainerTypes.NICKEL_IRON_BATTERY.get(), NickelIronBatteryScreen::new);
        MenuScreens.register(ModContainerTypes.ZINC_SILVER_BATTERY.get(), ZincSilverBatteryScreen::new);
        MenuScreens.register(ModContainerTypes.WATER_PUMP.get(), WaterPumpScreen::new);
        MenuScreens.register(ModContainerTypes.EXTRUDING_MACHINE.get(), ExtrudingMachineScreen::new);
        MenuScreens.register(ModContainerTypes.ROLLING_MACHINE.get(), RollingMachineScreen::new);
        MenuScreens.register(ModContainerTypes.ELECTRIC_FORGE.get(), ElectricForgeScreen::new);
        MenuScreens.register(ModContainerTypes.ELECTRIC_GRILL.get(), ElectricGrillScreen::new);
        MenuScreens.register(ModContainerTypes.FRIDGE.get(), FridgeScreen::new);
        MenuScreens.register(ModContainerTypes.CHARCOAL_KILN.get(), CharcoalKilnScreen::new);
        MenuScreens.register(ModContainerTypes.CANNING_PRESS.get(), CanningPressScreen::new);
        MenuScreens.register(ModContainerTypes.ELECTRIC_LOOM.get(), ElectricLoomScreen::new);
        MenuScreens.register(ModContainerTypes.SCRAPING_MACHINE.get(), ScrapingMachineScreen::new);
        MenuScreens.register(ModContainerTypes.BOILING_CAULDRON.get(), BoilingCauldronScreen::new);
        MenuScreens.register(ModContainerTypes.LAVA_BASIN.get(), LavaBasinScreen::new);
            //ITEM INVENTORY
        MenuScreens.register(ModContainerTypes.LEATHER_SATCHEL.get(), LeatherSatchelScreen::new);
        MenuScreens.register(ModContainerTypes.TOOL_BELT.get(), ToolBeltScreen::new);
            //ENTITY INVENTORY
        MenuScreens.register(ModContainerTypes.LOCOMOTIVE.get(), LocomotiveScreen::new);

        //CUSTOM ITEM PROPERTIES
        ModItemProperties.addCustomItemProperties();
    }
}
