package com.jewey.rosia.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS;
    public static final RegistryObject<MobEffect> PATH_SPEED;

    public ModEffects() {
    }

    public static <T extends MobEffect> RegistryObject<T> register(String name, Supplier<T> supplier) {
        return EFFECTS.register(name, supplier);
    }

    static {
        EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "rosia");
        PATH_SPEED = register("path_speed", () -> (new RosiaMobEffect(MobEffectCategory.BENEFICIAL, 5926017))
                .addAttributeModifier(Attributes.MOVEMENT_SPEED, "0e31b409-5bbe-44a8-a0df-f596c00897f3",
                        0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    public static class RosiaMobEffect extends MobEffect {
        public RosiaMobEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        public boolean isDurationEffectTick(int duration, int amplitude) {
            return this == ModEffects.PATH_SPEED.get();
        }
    }
}
