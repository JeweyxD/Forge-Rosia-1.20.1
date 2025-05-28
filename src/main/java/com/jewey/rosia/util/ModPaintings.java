package com.jewey.rosia.util;

import com.jewey.rosia.Rosia;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPaintings {
    public static final DeferredRegister<PaintingVariant> PAINTING_TYPES = DeferredRegister.create(Registries.PAINTING_VARIANT, Rosia.MOD_ID);
    public static final RegistryObject<PaintingVariant> MONOLITH = register("monolith", 48, 48);
    public static final RegistryObject<PaintingVariant> CREATOR = register("creator", 16, 16);

    private static RegistryObject<PaintingVariant> register(String name, int width, int height)
    {
        return PAINTING_TYPES.register(name, () -> new PaintingVariant(width, height));
    }
}
