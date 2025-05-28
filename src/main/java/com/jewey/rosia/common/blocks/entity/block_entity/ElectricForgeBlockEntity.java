package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.electric_forge;
import com.jewey.rosia.common.blocks.block.fire_box;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.ElectricForgeContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

import static com.jewey.rosia.Rosia.MOD_ID;
import static net.dries007.tfc.common.capabilities.heat.HeatCapability.adjustTempTowards;
import static net.dries007.tfc.common.capabilities.heat.HeatCapability.targetDeviceTemp;

public class ElectricForgeBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements ICalendarTickable, MenuProvider
{
    public static final int SLOT_INPUT_MIN = 0;
    public static final int SLOT_INPUT_MAX = 4;
    public static final int SLOT_EXTRA_MIN = 5;
    public static final int SLOT_EXTRA_MAX = 5;

    private final ItemStackHandler itemHandler = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    public @NotNull Component getDisplayName() {
        return NAME;
    }

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.electric_forge");


    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricForgeBlockEntity forge)
    {
        forge.checkForLastTickSync();
        forge.checkForCalendarUpdate();

        if (forge.needsRecipeUpdate)
        {
            forge.needsRecipeUpdate = false;
        }

        boolean isRaining = level.isRainingAt(pos);

        if (state.getValue(electric_forge.HEAT) > 0)
        {
            if (isRaining && level.random.nextFloat() < 0.15F)
            {
                Helpers.playSound(level, pos, SoundEvents.LAVA_EXTINGUISH);
            }
        }

        // Always update temperature until the forge is not hot anymore
        if (forge.temperature > 0 || forge.burnTemperature > 0)
        {
            forge.temperature = adjustDeviceTemp(forge.temperature, forge.burnTemperature, 0, isRaining);

            HeatCapability.provideHeatTo(level, pos.above(), forge.temperature);

            for (int i = SLOT_INPUT_MIN; i <= SLOT_INPUT_MAX; i++)
            {
                ItemStack stack = forge.inventory.getStackInSlot(i);
                int slot = i;
                stack.getCapability(HeatCapability.CAPABILITY).ifPresent(cap -> {
                    // Update temperature of item
                    float itemTemp = cap.getTemperature();
                    if (forge.temperature > itemTemp)
                    {
                        HeatCapability.addTemp(cap, forge.temperature, 6); //Heat items quicker (3 -> 6) 2x speed
                    }

                    // Handle possible melting, or conversion (if reach 1599 = pit kiln temperature)
                    forge.handleInputMelting(stack, slot);
                });
            }

            forge.markForSync();
        }

        // Update heat level
        if (state.getValue(electric_forge.HEAT) > 0 && forge.temperature > 0)
        {
            int heatLevel = Mth.clamp((int) (forge.temperature / 1550 * 6) + 1, 1, 7); // scaled 1 through 7
            if (heatLevel != state.getValue(electric_forge.HEAT))
            {
                level.setBlockAndUpdate(pos, state.setValue(electric_forge.HEAT, heatLevel));
                forge.markForSync();
            }
        }
        if(state.getValue(fire_box.HEAT) == 0 && forge.burnTemperature != 0)
        {
            level.setBlock(pos, state.setValue(electric_forge.HEAT, 1), Block.UPDATE_ALL);
        }
        if (state.getValue(electric_forge.HEAT) > 0 && forge.temperature == 0)
        {
            level.setBlockAndUpdate(pos, state.setValue(electric_forge.HEAT, 0));
        }

        // Consume energy
        if (forge.temperature > 0 && forge.burnTemperature > 0 && forge.burnTemperature >= forge.temperature)
        {
            int energyUse = Mth.clamp((int) ((forge.temperature / 1550) * 4), 1, 4);
            forge.ENERGY_STORAGE.extractEnergy(energyUse, false);

            forge.markForSync();
        }

        // Turn off when no energy remaining
        if (forge.ENERGY_STORAGE.getEnergyStored() <= 0)
        {
            forge.burnTemperature = 0;
            forge.markForSync();
        }
    }

    public InteractionResult upTemp(ServerPlayer player, int i){
        if (ENERGY_STORAGE.getEnergyStored() > 0) {
            burnTemperature = Mth.clamp(burnTemperature + i, 0, 1550);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    public InteractionResult downTemp(ServerPlayer player, int i){
        if (ENERGY_STORAGE.getEnergyStored() > 0) {
            burnTemperature = Mth.clamp(burnTemperature - i, 0, 1550);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    public InteractionResult setTemp0(ServerPlayer player, int i){
        burnTemperature = i;
        return InteractionResult.SUCCESS;
    }

    public static float adjustDeviceTemp(float temp, float baseTarget, int airTicks, boolean isRaining) {
        float target = targetDeviceTemp(baseTarget, airTicks, isRaining);
        if (temp != target) {
            float deltaPositive = 2.0F;     // Raise temperature 2x faster (ON)
            float deltaNegative = 6.0F;     // Lower temperature 6x faster (OFF)

            return adjustTempTowards(temp, target, deltaPositive, deltaNegative);
        } else {
            return target;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(1000, 50) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
        }
    };
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    public IEnergyStorage getEnergyStorage() {
        return ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
    }


    protected final ContainerData syncableData;
    private final HeatingRecipe[] cachedRecipes = new HeatingRecipe[5];
    private boolean needsSlotUpdate = false;
    private float temperature; // Current Temperature
    private float burnTemperature; // Temperature provided from the current item of fuel
    private long lastPlayerTick; // Last player tick this forge was ticked (for purposes of catching up)
    private boolean needsRecipeUpdate; // Set to indicate on tick, the cached recipes need to be re-updated

    public ElectricForgeBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ELECTRIC_FORGE_BLOCK_ENTITY.get(), pos, state, defaultInventory(6), NAME);

        temperature = 0;
        burnTemperature = 0;
        lastPlayerTick = Integer.MIN_VALUE;
        syncableData = new IntArrayBuilder().add(() -> (int) temperature, value -> temperature = value);

        Arrays.fill(cachedRecipes, null);

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(0, 1, 2, 3, 4), Direction.UP)
                .on(new PartialItemHandler(inventory).extract(0, 1, 2, 3, 4), Direction.DOWN)
                .on(new PartialItemHandler(inventory).insert(SLOT_EXTRA_MAX).extract(SLOT_EXTRA_MAX), Direction.Plane.HORIZONTAL);
    }

    @Override
    public void onCalendarUpdate(long ticks) {}

    @Override
    public long getLastCalendarUpdateTick() {
        return lastPlayerTick;
    }

    @Override
    public void setLastCalendarUpdateTick(long l) {
        lastPlayerTick = l;
    }


    public ContainerData getSyncableData()
    {
        return syncableData;
    }

    public float getTemperature()
    {
        return temperature;
    }
    public float getBurnTemperature()
    {
        return burnTemperature;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInv, Player player)
    {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        return ElectricForgeContainer.create(this, playerInv, windowID);
    }

    @Override
    public void onLoadAdditional() {
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        temperature = nbt.getFloat("temperature");
        burnTemperature = nbt.getFloat("burnTemperature");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putFloat("temperature", temperature);
        nbt.putFloat("burnTemperature", burnTemperature);
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        nbt.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        needsSlotUpdate = true;
        updateCachedRecipes();
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        if (slot <= SLOT_INPUT_MAX)
        {
            return Helpers.mightHaveCapability(stack, HeatCapability.CAPABILITY) && !Helpers.mightHaveCapability(stack, FoodCapability.CAPABILITY);
        }
        else
        {
            return Helpers.mightHaveCapability(stack, Capabilities.FLUID_ITEM, HeatCapability.CAPABILITY);
        }
    }

    private void handleInputMelting(ItemStack stack, int startIndex)
    {
        HeatingRecipe recipe = cachedRecipes[startIndex - SLOT_INPUT_MIN];
        stack.getCapability(HeatCapability.CAPABILITY).ifPresent(cap -> {
            if (recipe != null && recipe.isValidTemperature(cap.getTemperature()))
            {
                // Handle possible metal output
                final ItemStackInventory inventory = new ItemStackInventory(stack);
                FluidStack fluidStack = recipe.assembleFluid(inventory);
                ItemStack outputStack = recipe.assemble(inventory, level.registryAccess());
                float itemTemperature = cap.getTemperature();

                // Loop through all input slots
                for (int slot = SLOT_EXTRA_MIN; slot <= SLOT_EXTRA_MAX; slot++)
                {
                    fluidStack = Helpers.mergeOutputFluidIntoSlot(this.inventory, fluidStack, itemTemperature, slot);
                    if (fluidStack.isEmpty()) break;
                }

                FoodCapability.applyTrait(outputStack, FoodTraits.CHARCOAL_GRILLED); //Food shouldn't be allowed but... just in case
                this.inventory.setStackInSlot(startIndex, outputStack);
            }
        });
    }

    private void updateCachedRecipes()
    {
        // cache heat recipes for each input
        assert level != null;
        for (int i = SLOT_INPUT_MIN; i <= SLOT_INPUT_MAX; i++)
        {
            cachedRecipes[i - SLOT_INPUT_MIN] = null;
            ItemStack inputStack = inventory.getStackInSlot(i);
            if (!inputStack.isEmpty())
            {
                cachedRecipes[i - SLOT_INPUT_MIN] = HeatingRecipe.getRecipe(new ItemStackInventory(inputStack));
            }
        }
    }

    @Override
    public void clearContent() {

    }
}