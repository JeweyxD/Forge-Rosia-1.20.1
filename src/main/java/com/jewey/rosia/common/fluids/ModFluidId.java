package com.jewey.rosia.common.fluids;

import net.dries007.tfc.common.fluids.FluidId;
import net.minecraft.world.level.material.Fluid;

import java.util.OptionalInt;
import java.util.function.Supplier;

public record ModFluidId(String name, OptionalInt color, Supplier<? extends Fluid> fluid) {
    public static final FluidId INVAR_FLUID;
    public static final FluidId WEAK_PURPLE_STEEL_FLUID;
    public static final FluidId PURPLE_STEEL_FLUID;

    public String name() {
        return this.name;
    }
    public OptionalInt color() {
        return this.color;
    }
    public Supplier<? extends Fluid> fluid() {
        return this.fluid;
    }

    static {
        INVAR_FLUID = new FluidId("invar_fluid", OptionalInt.of(0xff695b43), ModFluids.INVAR_FLUID.source());
        WEAK_PURPLE_STEEL_FLUID = new FluidId("weak_purple_steel_fluid", OptionalInt.of(0xff523952), ModFluids.WEAK_PURPLE_STEEL_FLUID.source());
        PURPLE_STEEL_FLUID = new FluidId("purple_steel_fluid", OptionalInt.of(0xff694169), ModFluids.PURPLE_STEEL_FLUID.source());
    }
}
