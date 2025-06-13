package com.jewey.rosia.recipe;

import com.jewey.rosia.Rosia;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Rosia.MOD_ID);

    public static final RegistryObject<RecipeSerializer<ExtrudingMachineRecipe>> EXTRUDING_MACHINE_SERIALIZER =
            SERIALIZERS.register("extruding_machine", () -> ExtrudingMachineRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<RollingMachineRecipe>> ROLLING_MACHINE_SERIALIZER =
            SERIALIZERS.register("rolling_machine", () -> RollingMachineRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ElectricLoomRecipe>> ELECTRIC_LOOM_SERIALIZER =
            SERIALIZERS.register("electric_loom", () -> ElectricLoomRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ScrapingMachineRecipe>> SCRAPING_MACHINE_SERIALIZER =
            SERIALIZERS.register("scraping_machine", () -> ScrapingMachineRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<BoilingCauldronRecipe>> BOILING_CAULDRON_SERIALIZER =
            SERIALIZERS.register("boiling_cauldron", () -> BoilingCauldronRecipe.Serializer.INSTANCE);
}
