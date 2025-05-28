package com.jewey.rosia.integration.jei;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.items.ModItems;
import com.jewey.rosia.recipe.AutoQuernRecipe;
import com.jewey.rosia.recipe.RollingMachineRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class RollingMachineRecipeCategory implements IRecipeCategory<RollingMachineRecipe> {

    public final static ResourceLocation UID = new ResourceLocation(Rosia.MOD_ID, "rolling_machine");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/auto_quern_jei_2.png");

    public static final RecipeType<RollingMachineRecipe> ROLLING_MACHINE_TYPE =
            new RecipeType<>(UID, RollingMachineRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    protected final IDrawableStatic arrow;
    protected final IDrawableAnimated arrowAnimated;

    public RollingMachineRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 97, 26);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ROLLING_MACHINE.get()));

        this.arrow = helper.createDrawable(TEXTURE, 0, 27, 22, 16);
        IDrawableStatic arrowAnimated = helper.createDrawable(TEXTURE, 22, 27, 22, 16);
        this.arrowAnimated = helper.createAnimatedDrawable(arrowAnimated, 80, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void draw(RollingMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
        arrow.draw(stack, 48, 6);
        arrowAnimated.draw(stack, 48, 6);
    }

    @Override
    public RecipeType<RollingMachineRecipe> getRecipeType() {
        return ROLLING_MACHINE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.rosia.rolling_machine");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull RollingMachineRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 25, 5).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addIngredients(Ingredient.of(ModItems.STEEL_ROLLERS.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 5).addItemStack(recipe.getResultItem(null));
    }
}
