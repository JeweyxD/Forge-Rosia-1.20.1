package com.jewey.rosia.integration.jei;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.recipe.BoilingCauldronRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dries007.tfc.common.recipes.ingredients.ItemStackIngredient;
import net.dries007.tfc.compat.jei.JEIIntegration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

import static net.dries007.tfc.compat.jei.category.BaseRecipeCategory.collapse;

public class BoilingCauldronRecipeCategory implements IRecipeCategory<BoilingCauldronRecipe> {

    public final static ResourceLocation UID = new ResourceLocation(Rosia.MOD_ID, "boiling_cauldron");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/boiling_cauldron_one_output_jei.png");

    public static final RecipeType<BoilingCauldronRecipe> BOILING_CAULDRON_TYPE =
            new RecipeType<>(UID, BoilingCauldronRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    protected final IDrawableStatic slot;
    protected final IDrawableStatic fire;
    protected final IDrawableAnimated fireAnimated;

    public BoilingCauldronRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 97, 45);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.BOILING_CAULDRON.get()));

        this.slot = helper.getSlotDrawable();
        this.fire = helper.createDrawable(TEXTURE, 99, 0, 14, 14);
        IDrawableStatic fireAnimated = helper.createDrawable(TEXTURE, 99, 15, 14, 14);
        this.fireAnimated = helper.createAnimatedDrawable(fireAnimated, 160, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    public void draw(BoilingCauldronRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
        fire.draw(stack, 41, 16);
        fireAnimated.draw(stack, 41, 16);
    }

    @Override
    public RecipeType<BoilingCauldronRecipe> getRecipeType() {
        return BOILING_CAULDRON_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.rosia.boiling_cauldron");
    }

    @SuppressWarnings("removal")
    @Override
    public IDrawable getBackground() {
        return this.background;
    }


    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull BoilingCauldronRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        //Item input
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addItemStacks(collapse(new ItemStackIngredient(recipe.getIngredients().get(0), recipe.getInputCount())));

        //Fluid input
        final FluidStack inputFluid = recipe.getFluidStackInput();
        if (!inputFluid.isEmpty())
        {
            IRecipeSlotBuilder fluidOutput = builder.addSlot(RecipeIngredientRole.INPUT, 5, 25);
            fluidOutput.addIngredient(JEIIntegration.FLUID_STACK, inputFluid);
            fluidOutput.setFluidRenderer(1, false, 16, 16);
            fluidOutput.setBackground(slot, -1, -1);
        }

        //Fluid output
        final FluidStack outputFluid = recipe.getFluidStackOutput();
        if (!outputFluid.isEmpty())
        {
            IRecipeSlotBuilder fluidOutput = builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 15);
            fluidOutput.addIngredient(JEIIntegration.FLUID_STACK, outputFluid);
            fluidOutput.setFluidRenderer(1, false, 16, 16);
            fluidOutput.setBackground(slot, -1, -1);
        }

        //Item output
        if (recipe.getResultItem(null).getCount() != 0)
        {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 15).addItemStack(recipe.getResultItem(null));
        }
    }
}
