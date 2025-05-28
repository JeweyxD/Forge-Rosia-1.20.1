package com.jewey.rosia.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jewey.rosia.Rosia;
import net.dries007.tfc.util.JsonHelpers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BoilingCauldronRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final int inputCount;
    private final NonNullList<Ingredient> itemIngredient;
    private final FluidStack fluidStackInput;
    private final FluidStack fluidStackOutput;
    protected final int duration;
    protected final float minTemp;

    @Override
    public boolean isSpecial() {
        return true;
    }

    public BoilingCauldronRecipe(ResourceLocation id, ItemStack output, int inputCount, NonNullList<Ingredient> itemIngredient, FluidStack fluidStackInput, FluidStack fluidStackOutput, int duration, float minTemp) {
        this.id = id;
        this.output = output;
        this.inputCount = inputCount;
        this.itemIngredient = itemIngredient;
        this.fluidStackInput = fluidStackInput;
        this.fluidStackOutput = fluidStackOutput;
        this.duration = duration;
        this.minTemp = minTemp;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel)
    {
        if(pLevel.isClientSide()) {
            return false;
        }
        //Check if recipe has an item input; if not, check if recipe has an item output
        //Recipe must have either an input or output item AND/OR recipe must have either an input item or input fluid
        if (!pContainer.getItem(1).isEmpty())
        {
            return itemIngredient.get(0).test(pContainer.getItem(1));
        }
        else return inputCount == 0 && output.getCount() != 0;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return itemIngredient;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    public int getInputCount()
    {
        return inputCount;
    }
    public int getDuration()
    {
        return duration;
    }
    public float getMinTemp()
    {
        return minTemp;
    }
    public FluidStack getFluidStackInput()
    {
        return fluidStackInput;
    }
    public FluidStack getFluidStackOutput()
    {
        return fluidStackOutput;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<BoilingCauldronRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "boiling_cauldron";
    }

    public static class Serializer implements RecipeSerializer<BoilingCauldronRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(Rosia.MOD_ID,"boiling_cauldron");

        @Override
        public BoilingCauldronRecipe fromJson(ResourceLocation id, JsonObject json) {
            final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);

            final int inputCount = JsonHelpers.getAsInt(json, "input_count");

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            final FluidStack fluidStackInput = json.has("input_fluid") ? JsonHelpers.getFluidStack(JsonHelpers.getAsJsonObject(json, "input_fluid")) : FluidStack.EMPTY;
            final FluidStack fluidStackOutput = json.has("output_fluid") ? JsonHelpers.getFluidStack(JsonHelpers.getAsJsonObject(json, "output_fluid")) : FluidStack.EMPTY;
            final int duration = GsonHelper.getAsInt(json, "duration");
            final float minTemp = GsonHelper.getAsFloat(json, "temperature");

            return new BoilingCauldronRecipe(id, output, inputCount, inputs, fluidStackInput, fluidStackOutput, duration, minTemp);
        }

        @Override
        public BoilingCauldronRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            ItemStack output = buf.readItem();

            final int inputCount = buf.readVarInt();

            final FluidStack fluidStackInput = FluidStack.readFromPacket(buf);
            final FluidStack fluidStackOutput = FluidStack.readFromPacket(buf);
            final int duration = buf.readVarInt();
            final float minTemp = buf.readFloat();

            return new BoilingCauldronRecipe(id, output, inputCount, inputs, fluidStackInput, fluidStackOutput, duration, minTemp);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, BoilingCauldronRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(null), false);
            buf.writeVarInt(recipe.inputCount);
            recipe.fluidStackInput.writeToPacket(buf);
            recipe.fluidStackOutput.writeToPacket(buf);
            buf.writeVarInt(recipe.duration);
            buf.writeFloat(recipe.minTemp);
        }


        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}
