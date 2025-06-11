package com.jewey.rosia.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jewey.rosia.Rosia;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.JsonHelpers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RollingMachineRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStackProvider output;
    private final NonNullList<Ingredient> recipeItems;

    @Override
    public boolean isSpecial() {
        return true;
    }

    public RollingMachineRecipe(ResourceLocation id, ItemStackProvider output, NonNullList<Ingredient> recipeItems) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, @NotNull Level pLevel) {
        if(pLevel.isClientSide()) {
            return false;
        }
        return recipeItems.get(0).test(pContainer.getItem(1));
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.getEmptyStack();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.getEmptyStack();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<RollingMachineRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "rolling_machine";
    }

    public static class Serializer implements RecipeSerializer<RollingMachineRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(Rosia.MOD_ID,"rolling_machine");

        @Override
        public @NotNull RollingMachineRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            ItemStackProvider output = ItemStackProvider.fromJson(JsonHelpers.getAsJsonObject(json, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            return new RollingMachineRecipe(id, output, inputs);
        }

        @Override
        public RollingMachineRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            ItemStackProvider output = ItemStackProvider.fromNetwork(buf);
            return new RollingMachineRecipe(id, output, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, RollingMachineRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            recipe.output.toNetwork(buf);
            //buf.writeItemStack(recipe.getResultItem(null), false);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}
