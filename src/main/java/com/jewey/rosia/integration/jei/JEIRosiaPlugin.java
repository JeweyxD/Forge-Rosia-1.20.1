package com.jewey.rosia.integration.jei;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.recipe.*;
import com.jewey.rosia.screen.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.recipes.QuernRecipe;
import net.dries007.tfc.compat.jei.category.QuernRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEIRosiaPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Rosia.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        final IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AutoQuernRecipeCategory(gui));
        registration.addRecipeCategories(new ExtrudingMachineRecipeCategory(gui));
        registration.addRecipeCategories(new RollingMachineRecipeCategory(gui));
        registration.addRecipeCategories(new ElectricLoomRecipeCategory(gui));
        registration.addRecipeCategories(new ScrapingMachineRecipeCategory(gui));
        registration.addRecipeCategories(new BoilingCauldronRecipeCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<AutoQuernRecipe> autoQuernRecipes = recipeManager.getAllRecipesFor(AutoQuernRecipe.Type.INSTANCE);
        registration.addRecipes(AutoQuernRecipeCategory.AUTO_QUERN_TYPE, autoQuernRecipes);

        List<ExtrudingMachineRecipe> extrudingMachineRecipes = recipeManager.getAllRecipesFor(ExtrudingMachineRecipe.Type.INSTANCE);
        registration.addRecipes(ExtrudingMachineRecipeCategory.EXTRUDING_MACHINE_TYPE, extrudingMachineRecipes);

        List<RollingMachineRecipe> rollingMachineRecipes = recipeManager.getAllRecipesFor(RollingMachineRecipe.Type.INSTANCE);
        registration.addRecipes(RollingMachineRecipeCategory.ROLLING_MACHINE_TYPE, rollingMachineRecipes);

        List<ElectricLoomRecipe> electricLoomRecipes = recipeManager.getAllRecipesFor(ElectricLoomRecipe.Type.INSTANCE);
        registration.addRecipes(ElectricLoomRecipeCategory.ELECTRIC_LOOM_TYPE, electricLoomRecipes);

        List<ScrapingMachineRecipe> scrapingMachineRecipes = recipeManager.getAllRecipesFor(ScrapingMachineRecipe.Type.INSTANCE);
        registration.addRecipes(ScrapingMachineRecipeCategory.SCRAPING_MACHINE_TYPE, scrapingMachineRecipes);

        List<BoilingCauldronRecipe> boilingCauldronRecipes = recipeManager.getAllRecipesFor(BoilingCauldronRecipe.Type.INSTANCE);
        registration.addRecipes(BoilingCauldronRecipeCategory.BOILING_CAULDRON_TYPE, boilingCauldronRecipes);
    }

    public static final RecipeType<QuernRecipe> QUERN = type("quern", QuernRecipe.class);
    private static <T> RecipeType<T> type(String name, Class<T> tClass)
    {
        return RecipeType.create(TerraFirmaCraft.MOD_ID, name, tClass);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addRecipeClickArea(AutoQuernScreen.class, 86, 48, 22, 15, QUERN);
        registration.addRecipeClickArea(ExtrudingMachineScreen.class, 86, 48, 22, 15, ExtrudingMachineRecipeCategory.EXTRUDING_MACHINE_TYPE);
        registration.addRecipeClickArea(RollingMachineScreen.class, 86, 48, 22, 15, RollingMachineRecipeCategory.ROLLING_MACHINE_TYPE);
        registration.addRecipeClickArea(ElectricLoomScreen.class, 86, 48, 22, 15, ElectricLoomRecipeCategory.ELECTRIC_LOOM_TYPE);
        registration.addRecipeClickArea(ScrapingMachineScreen.class, 86, 48, 22, 15, ScrapingMachineRecipeCategory.SCRAPING_MACHINE_TYPE);
        registration.addRecipeClickArea(BoilingCauldronScreen.class, 91, 67, 16, 7, BoilingCauldronRecipeCategory.BOILING_CAULDRON_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.AUTO_QUERN.get()), QUERN);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EXTRUDING_MACHINE.get()), ExtrudingMachineRecipeCategory.EXTRUDING_MACHINE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROLLING_MACHINE.get()), RollingMachineRecipeCategory.ROLLING_MACHINE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ELECTRIC_LOOM.get()), ElectricLoomRecipeCategory.ELECTRIC_LOOM_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SCRAPING_MACHINE.get()), ScrapingMachineRecipeCategory.SCRAPING_MACHINE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BOILING_CAULDRON.get()), BoilingCauldronRecipeCategory.BOILING_CAULDRON_TYPE);
    }
}
