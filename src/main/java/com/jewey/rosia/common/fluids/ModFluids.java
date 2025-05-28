package com.jewey.rosia.common.fluids;


import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import net.dries007.tfc.common.fluids.*;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS
            = DeferredRegister.create(Registries.FLUID, Rosia.MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES
            = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Rosia.MOD_ID);

    public static final ResourceLocation MOLTEN_STILL = Helpers.identifier("block/molten_still");
    public static final ResourceLocation MOLTEN_FLOW = Helpers.identifier("block/molten_flow");
    public static final int ALPHA_MASK = 0xFF000000;

    private static FluidType.Properties lavaLike()
    {
        return FluidType.Properties.create()
                .adjacentPathType(BlockPathTypes.LAVA)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .lightLevel(15)
                .density(3000)
                .viscosity(6000)
                .temperature(1300)
                .canConvertToSource(false)
                .canDrown(false)
                .canExtinguish(false)
                .canHydrate(false)
                .canPushEntity(false)
                .canSwim(false)
                .supportsBoating(false);
    }

    public static final FluidRegistryObject<ForgeFlowingFluid> INVAR_FLUID = register(
            "invar_fluid",
            properties -> properties
                    .block(ModBlocks.INVAR_FLUID)
                    .bucket(TFCItems.FLUID_BUCKETS.get(ModFluidId.INVAR_FLUID))
                    .explosionResistance(100),
            lavaLike()
                    .descriptionId("fluid.rosia.invar_fluid")
                    .rarity(Rarity.COMMON),
            new FluidTypeClientProperties(ALPHA_MASK | 0xff695b43, MOLTEN_STILL, MOLTEN_FLOW, null, null),
            MoltenFluid.Source::new,
            MoltenFluid.Flowing::new
    );

    public static final FluidRegistryObject<ForgeFlowingFluid> WEAK_PURPLE_STEEL_FLUID = register(
            "weak_purple_steel_fluid",
            properties -> properties
                    .block(ModBlocks.WEAK_PURPLE_STEEL_FLUID)
                    .bucket(TFCItems.FLUID_BUCKETS.get(ModFluidId.WEAK_PURPLE_STEEL_FLUID))
                    .explosionResistance(100),
            lavaLike()
                    .descriptionId("fluid.rosia.weak_purple_steel_fluid")
                    .rarity(Rarity.COMMON),
            new FluidTypeClientProperties(ALPHA_MASK | 0xff523952, MOLTEN_STILL, MOLTEN_FLOW, null, null),
            MoltenFluid.Source::new,
            MoltenFluid.Flowing::new
    );

    public static final FluidRegistryObject<ForgeFlowingFluid> PURPLE_STEEL_FLUID = register(
            "purple_steel_fluid",
            properties -> properties
                    .block(ModBlocks.PURPLE_STEEL_FLUID)
                    .bucket(TFCItems.FLUID_BUCKETS.get(ModFluidId.PURPLE_STEEL_FLUID))
                    .explosionResistance(100),
            lavaLike()
                    .descriptionId("fluid.rosia.purple_steel_fluid")
                    .rarity(Rarity.COMMON),
            new FluidTypeClientProperties(ALPHA_MASK | 0xff694169, MOLTEN_STILL, MOLTEN_FLOW, null, null),
            MoltenFluid.Source::new,
            MoltenFluid.Flowing::new
    );

    private static <F extends FlowingFluid> FluidRegistryObject<F> register(String name, Consumer<ForgeFlowingFluid.Properties> builder, FluidType.Properties typeProperties, FluidTypeClientProperties clientProperties, Function<ForgeFlowingFluid.Properties, F> sourceFactory, Function<ForgeFlowingFluid.Properties, F> flowingFactory)
    {

        final String flowingName = name + "_flowing";

        return RegistrationHelpers.registerFluid(FLUID_TYPES, FLUIDS, name, name, flowingName, builder,
                () -> new ExtendedFluidType(typeProperties, clientProperties), sourceFactory, flowingFactory);
    }
}
