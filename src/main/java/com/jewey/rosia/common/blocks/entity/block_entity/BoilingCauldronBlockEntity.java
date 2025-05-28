package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.boiling_cauldron;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.BoilingCauldronContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.FluidSyncS2CPacket;
import com.jewey.rosia.recipe.BoilingCauldronRecipe;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.SidedHandler;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeatBlock;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.jewey.rosia.Rosia.MOD_ID;

public class BoilingCauldronBlockEntity extends TickableInventoryBlockEntity<BoilingCauldronBlockEntity.BoilingCauldronInventory> implements ICalendarTickable
{

    public @NotNull Component getDisplayName() {
        return NAME;
    }
    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOT_INGREDIENT_IN = 1;
    public static final int SLOTS = 2;

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.boiling_cauldron");
    private static final int TARGET_TEMPERATURE_STABILITY_TICKS = 5;

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilingCauldronBlockEntity cauldron) {
        cauldron.checkForLastTickSync();
        cauldron.checkForCalendarUpdate();

        if (cauldron.needsRecipeUpdate) {
            cauldron.needsRecipeUpdate = false;
        }

        //Temperature control
        if (cauldron.temperature != cauldron.targetTemperature) {
            cauldron.temperature = HeatCapability.adjustTempTowards(cauldron.temperature, cauldron.targetTemperature);
        }
        if (cauldron.targetTemperatureStabilityTicks > 0) {
            cauldron.targetTemperatureStabilityTicks--;
        }
        if (cauldron.targetTemperature > 0 && cauldron.targetTemperatureStabilityTicks == 0) {
            // Cauldron target temperature decays constantly, since it is set externally. As long as we don't consider ourselves 'stable' (received an external setTemperature() call within the last 5 ticks)
            cauldron.targetTemperature = HeatCapability.adjustTempTowards(cauldron.targetTemperature, 0);
        }

        //Boiling
        if(hasRecipe(cauldron))
        {
            if (cauldron.progress > 0)
            {
                cauldron.progress--;
            }
            level.setBlock(pos, state.setValue(boiling_cauldron.LIT, true), 3);
            if (cauldron.progress <= 1)
            {
                finishBoiling(cauldron);
            }
        }
        else
        {
            cauldron.progress = 0;
            level.setBlock(pos, state.setValue(boiling_cauldron.LIT, false), 3);
        }

        //Transfer fluid from item in slot 0 to FLUID_TANK
        if(hasFluidItemInSourceSlot(cauldron)) {
            transferItemFluidToFluidTank(cauldron);
        }
    }

    private int progress;

    public int getProgress()
    {
        return progress;
    }

    private static boolean hasRecipe(BoilingCauldronBlockEntity cauldron)
    {
        Level level = cauldron.level;
        SimpleContainer inventory = new SimpleContainer(cauldron.inventory.getSlots());
        for (int i = 0; i < cauldron.inventory.getSlots(); i++)
        {
            inventory.setItem(i, cauldron.inventory.getStackInSlot(i));
        }

        Optional<BoilingCauldronRecipe> match = level.getRecipeManager()
                .getRecipeFor(BoilingCauldronRecipe.Type.INSTANCE, inventory, level);

        //Check all parts of the recipe are present at their minimum values
        boolean hasCount = false;
        boolean hasTemp = false;
        boolean hasFluid = false;
        boolean hasFluidAmount = false;
        if (match.isPresent())
        {
            //Check recipe for its input item; if no input item, no item allowed in slot
            int count = match.get().getInputCount();
            int slotCount = cauldron.inventory.getStackInSlot(1).isEmpty() ? 0 : cauldron.inventory.getStackInSlot(1).getCount();
            hasCount = count != 0 ? slotCount >= count : slotCount == 0;
            //Check recipe for min temperature
            float temp = match.get().getMinTemp();
            hasTemp = temp <= cauldron.getTemperature();
            //Check recipe for its input fluid, match with fluid in tank; if no input fluid, no fluid allowed in tank
            FluidStack fluidStack = match.get().getFluidStackInput();
            hasFluid = match.get().getFluidStackInput().getAmount() == 0 || cauldron.FLUID_TANK.getFluid().containsFluid(fluidStack);
            //Check recipe for its input fluid; if no input fluid, no fluid allowed in tank
            var tank = cauldron.FLUID_TANK.getFluidInTank(0).getAmount();
            hasFluidAmount = fluidStack.getAmount() != 0 ? tank >= fluidStack.getAmount() : tank == 0;

            //If all true and progress hasn't been set, set progress as recipe duration
            if (hasCount && hasTemp && hasFluid && hasFluidAmount && cauldron.progress == 0) {
                cauldron.progress = match.get().getDuration();
            }
        }

        return match.isPresent() && hasCount && hasTemp && hasFluid && hasFluidAmount;
    }

    private static void finishBoiling(BoilingCauldronBlockEntity cauldron)
    {
        Level level = cauldron.level;
        SimpleContainer inventory = new SimpleContainer(cauldron.inventory.getSlots());
        for (int i = 0; i < cauldron.inventory.getSlots(); i++)
        {
            inventory.setItem(i, cauldron.inventory.getStackInSlot(i));
        }

        Optional<BoilingCauldronRecipe> match = level.getRecipeManager()
                .getRecipeFor(BoilingCauldronRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent())
        {
            //Prevent stack overflow and limit consumption of inputs to prevent item/fluid consumption on overflow
            int bulkCount = getBulkCount(cauldron);
            int newBulkCount;
            double maxItemBulk = 0;
            double maxFluidBulk = 0;
            //Check if item output would cause stack overflow, return the max bulk recipe count that would not overflow
            if ((bulkCount * match.get().getResultItem(null).getCount()) > match.get().getResultItem(null).getMaxStackSize())
            {
                int a = (bulkCount * match.get().getResultItem(null).getCount()) - match.get().getResultItem(null).getMaxStackSize();
                int b = a / match.get().getInputCount();
                int c = (int) Math.ceil(b);
                maxItemBulk = bulkCount - c;
            }
            //Check if fluid output would cause stack overflow, return the max bulk recipe count that would not overflow
            if ((bulkCount * match.get().getFluidStackOutput().getAmount()) > cauldron.FLUID_TANK.getTankCapacity(0))
            {
                int a = (bulkCount * match.get().getFluidStackOutput().getAmount()) - cauldron.FLUID_TANK.getTankCapacity(0);
                int b = a / match.get().getFluidStackOutput().getAmount();
                int c = (int) Math.ceil(b);
                maxFluidBulk = bulkCount - c;
            }
            //Check if either item or fluid output would overflow with default bulk count, return new count to prevent overflow
            if (maxItemBulk != 0 && maxFluidBulk != 0)
            {
                newBulkCount = (int) Math.min(maxItemBulk, maxFluidBulk);
            }
            else if (maxItemBulk != 0 || maxFluidBulk != 0)
            {
                newBulkCount = (int) Math.max(maxItemBulk, maxFluidBulk);
            }
            else newBulkCount = bulkCount;


            //Extract only what was used in the recipe
            cauldron.inventory.extractItem(1, match.get().getInputCount() * newBulkCount, false);

            //Will overwrite any excess input items if output item exists
            if (match.get().getResultItem(null).getCount() != 0)
            {
                cauldron.inventory.setStackInSlot(1, new ItemStack(match.get().getResultItem(null).getItem(),
                        Math.min(match.get().getResultItem(null).getCount() * newBulkCount, match.get().getResultItem(null).getMaxStackSize())));
            }

            //If recipe has output fluid, empty tank first, then set new fluidStack
            if (match.get().getFluidStackOutput().getAmount() != 0)
            {
                cauldron.FLUID_TANK.setFluid(FluidStack.EMPTY);
                FluidStack fluidStack = new FluidStack(match.get().getFluidStackOutput().getFluid(),
                        Math.min(match.get().getFluidStackOutput().getAmount() * newBulkCount, cauldron.FLUID_TANK.getCapacity()));
                cauldron.FLUID_TANK.setFluid(fluidStack);
            }
            //This is for a recipe with a fluid input, item output, but no fluid output
            else cauldron.FLUID_TANK.drain(match.get().getFluidStackInput().getAmount() * newBulkCount, IFluidHandler.FluidAction.EXECUTE);
        }
        //Reset progress
        cauldron.progress = 0;
    }

    private static int getBulkCount(BoilingCauldronBlockEntity cauldron)
    {
        Level level = cauldron.level;
        SimpleContainer inventory = new SimpleContainer(cauldron.inventory.getSlots());
        for (int i = 0; i < cauldron.inventory.getSlots(); i++)
        {
            inventory.setItem(i, cauldron.inventory.getStackInSlot(i));
        }

        Optional<BoilingCauldronRecipe> match = level.getRecipeManager()
                .getRecipeFor(BoilingCauldronRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent())
        {
            //Determine the raw max number of recipes the current inventory can process

            //Return arbitrary large number to prevent a false 0 output if recipe does not require input fluid
            int a = cauldron.FLUID_TANK.getFluidInTank(0).isEmpty() ? 100 : cauldron.FLUID_TANK.getFluidInTank(0).getAmount();
            //The recipe may not have an input fluid, prevent divide by 0
            int b = match.get().getFluidStackInput().getAmount() != 0 ? match.get().getFluidStackInput().getAmount() : 1;
            double maxFluidRecipe = Math.floor(a / b);
            //Return arbitrary large number to prevent a false 0 output if recipe does not require input item
            int c = cauldron.inventory.getStackInSlot(1).isEmpty() ? 100 : cauldron.inventory.getStackInSlot(1).getCount();
            //The recipe may not have an input item, prevent divide by 0
            int d = match.get().getInputCount() != 0 ? match.get().getInputCount() : 1;
            double maxItemRecipe = Math.floor(c / d);

            //If the recipe has neither input item nor input fluid, 100 is returned which would break the output
            //However, if neither inputs exist, the recipe would not be able to be found in the first place so who cares
            //Basically don't be an idiot and make a recipe that converts nothing into something because straight to jail
            int minRecipe = (int) Math.min(maxFluidRecipe, maxItemRecipe);
            //Should only be called when a recipe is valid, therefor always returns minimum of 1 when called
            return Math.max(minRecipe, 1);
        }
        else return 1;
    }


    private static void transferItemFluidToFluidTank(BoilingCauldronBlockEntity cauldron) {
        cauldron.inventory.getStackInSlot(0).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int drainAmount = Math.min(cauldron.FLUID_TANK.getSpace(), 1000);

            FluidStack stack = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            //Check if the fluid is valid and is the same as any current fluid in tank
            if(cauldron.FLUID_TANK.isFluidValid(stack)
                    && (cauldron.FLUID_TANK.getFluid().containsFluid(stack) || cauldron.FLUID_TANK.getFluid().containsFluid(FluidStack.EMPTY))) {
                stack = handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
                fillTankWithFluid(cauldron, stack, handler.getContainer());
            }
        });
    }

    private static void fillTankWithFluid(BoilingCauldronBlockEntity cauldron, FluidStack stack, ItemStack container) {
        cauldron.FLUID_TANK.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        cauldron.inventory.extractItem(0, 1, false);
        cauldron.inventory.insertItem(0, container, false);
    }

    private static boolean hasFluidItemInSourceSlot(BoilingCauldronBlockEntity cauldron) {
        return cauldron.inventory.getStackInSlot(0).getCount() > 0;
    }


    private final SidedHandler.Noop<IHeatBlock> sidedHeat;
    private final IntArrayBuilder syncableData;
    private float temperature;
    private float targetTemperature;
    private boolean needsRecipeUpdate;

    /**
     * Prevent the target temperature from "hovering" around a particular value.
     * Effectively means that setTemperature() sets for the next 5 ticks, before it starts to decay naturally.
     */
    private int targetTemperatureStabilityTicks;
    private int lastFillTicks;
    private long lastUpdateTick; // for ICalendarTickable

    public BoilingCauldronBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.BOILING_CAULDRON_BLOCK_ENTITY.get(), pos, state, BoilingCauldronInventory::new, NAME);

        needsRecipeUpdate = true;
        temperature = targetTemperature = 0;
        lastFillTicks = 0;
        lastUpdateTick = Integer.MIN_VALUE;


        // Heat can be accessed from all sides
        sidedHeat = new SidedHandler.Noop<>(inventory);

        syncableData = new IntArrayBuilder()
                .add(() -> (int) temperature, value -> temperature = value);
    }

    public float getTemperature()
    {
        return temperature;
    }

    public ContainerData getSyncableData()
    {
        return syncableData;
    }


    @Override
    public void onCalendarUpdate(long ticks)
    {
        assert level != null;

        targetTemperature = HeatCapability.adjustTempTowards(targetTemperature, 0, ticks);
        temperature = HeatCapability.adjustTempTowards(temperature, targetTemperature, ticks);
    }

    @Override
    public long getLastCalendarUpdateTick() {
        return lastUpdateTick;
    }

    @Override
    public void setLastCalendarUpdateTick(long l) {
        lastUpdateTick = l;
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return switch (slot) {
            case 0 -> 1;
            default -> super.getSlotStackLimit(slot);
        };
    }


    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        ModMessages.sendToClients(new FluidSyncS2CPacket(this.getFluidStack(), worldPosition));
        return BoilingCauldronContainer.create(this, player.getInventory(), containerId);
    }


    @Override
    public void onLoadAdditional() {
        super.onLoadAdditional();
        lazyFluidHandler = LazyOptional.of(() -> FLUID_TANK);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        temperature = nbt.getFloat("temperature");
        targetTemperature = nbt.getFloat("targetTemperature");
        targetTemperatureStabilityTicks = nbt.getInt("targetTemperatureStabilityTicks");
        lastUpdateTick = nbt.getLong("lastUpdateTick");
        needsRecipeUpdate = true;
        FLUID_TANK.readFromNBT(nbt);
        progress = nbt.getInt("progress");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putFloat("temperature", temperature);
        nbt.putFloat("targetTemperature", targetTemperature);
        nbt.putInt("targetTemperatureStabilityTicks", targetTemperatureStabilityTicks);
        nbt.putLong("lastUpdateTick", lastUpdateTick);
        nbt = FLUID_TANK.writeToNBT(nbt);
        nbt.putInt("progress", progress);
        super.saveAdditional(nbt);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        if (cap == HeatCapability.BLOCK_CAPABILITY)
        {
            return sidedHeat.getSidedHandler(side).cast();
        }
        return super.getCapability(cap, side);
    }


    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for hopper interaction
            return false;
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for player interaction
        return switch (slot) {
            case 0 -> stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent() || stack.getItem() instanceof BucketItem;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public void clearContent() {

    }

    public static class BoilingCauldronInventory implements DelegateItemHandler, INBTSerializable<CompoundTag>, IHeatBlock
    {
        private final BoilingCauldronBlockEntity cauldron;

        private final InventoryItemHandler inventory;

        BoilingCauldronInventory(InventoryBlockEntity<?> entity)
        {
            cauldron = (BoilingCauldronBlockEntity) entity;
            inventory = new InventoryItemHandler(entity, SLOTS);
        }

        @Override
        public IItemHandlerModifiable getItemHandler()
        {
            return inventory;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            nbt.put("inventory", inventory.serializeNBT());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override
        public float getTemperature()
        {
            return cauldron.temperature;
        }

        @Override
        public void setTemperature(float temperature)
        {
            cauldron.targetTemperature = temperature;
            cauldron.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
            cauldron.markForSync();
        }

        @Override
        public void setTemperatureIfWarmer(float temperature)
        {
            // Override to still cause an update to the stability ticks
            if (temperature >= cauldron.temperature)
            {
                cauldron.temperature = temperature;
                cauldron.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
                cauldron.markForSync();
            }
        }

    }

    /**
     * Fluid stuff
     */


    private final FluidTank FLUID_TANK = new FluidTank(5000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if(!level.isClientSide()) {
                ModMessages.sendToClients(new FluidSyncS2CPacket(this.fluid, worldPosition));
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().is(TFCTags.Fluids.USABLE_IN_POT);
        }
    };

    public void setFluid(FluidStack stack){
        this.FLUID_TANK.setFluid(stack);
    }

    public FluidStack getFluidStack() {
        return this.FLUID_TANK.getFluid();
    }

    public IFluidHandler getFluidStorage() {
        return FLUID_TANK;
    }

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public float render() {
        return (float) FLUID_TANK.getFluidAmount() / (float) FLUID_TANK.getCapacity();
    }

}


