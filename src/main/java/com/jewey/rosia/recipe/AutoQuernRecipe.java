package com.jewey.rosia.recipe;

import net.dries007.tfc.common.recipes.QuernRecipe;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;

public class AutoQuernRecipe extends QuernRecipe {

    public AutoQuernRecipe(ResourceLocation id, Ingredient ingredient, ItemStackProvider result) {
        super(id, ingredient, result);
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.AUTO_QUERN_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<AutoQuernRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "auto_quern";
    }
}
