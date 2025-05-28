package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.steam_generator;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.SteamGeneratorContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.networking.packet.FluidSyncS2CPacket;
import com.jewey.rosia.util.ModEnergyStorage;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.jewey.rosia.Rosia.MOD_ID;

public class SteamGeneratorBlockEntity extends TickableInventoryBlockEntity<SteamGeneratorBlockEntity.SteamGeneratorInventory> implements ICalendarTickable
{

    public @NotNull Component getDisplayName() {
        return NAME;
    }
    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOTS = 1;

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.steam_generator");
    private static final int TARGET_TEMPERATURE_STABILITY_TICKS = 5;


    public static void serverTick(Level level, BlockPos pos, BlockState state, SteamGeneratorBlockEntity generator) {
        generator.checkForLastTickSync();
        generator.checkForCalendarUpdate();

        if (generator.needsRecipeUpdate) {
            generator.needsRecipeUpdate = false;
        }

        if (generator.temperature != generator.targetTemperature) {
            generator.temperature = HeatCapability.adjustTempTowards(generator.temperature, generator.targetTemperature);
        }
        if (generator.targetTemperatureStabilityTicks > 0) {
            generator.targetTemperatureStabilityTicks--;
        }
        if (generator.targetTemperature > 0 && generator.targetTemperatureStabilityTicks == 0) {
            // generator target temperature decays constantly, since it is set externally. As long as we don't consider ourselves 'stable' (received an external setTemperature() call within the last 5 ticks)
            generator.targetTemperature = HeatCapability.adjustTempTowards(generator.targetTemperature, 0);
        }

    //Power Generation
        if (generator.temperature >= 100 && hasEnoughFluid(generator)
                && generator.ENERGY_STORAGE.getEnergyStored() < generator.ENERGY_STORAGE.getMaxEnergyStored()) {

            int maxCap = generator.ENERGY_STORAGE.getMaxEnergyStored();
            // Higher temperatures = faster power generation
            if(generator.temperature >= 1951) {
                // 5 FE/tick 1951++
                // max temp for charcoal/lignite & bellows = 1950C
                // max temp for bituminous coal & bellows = 2015C
                // this aims to grant a bonus to players using "high quality" bituminous coal fuel
                int X = 5;
                generator.setEnergyLevel(Math.min(generator.ENERGY_STORAGE.getEnergyStored() + X, maxCap));
                generator.FLUID_TANK.drain(2 * X, IFluidHandler.FluidAction.EXECUTE);
            } else if(generator.temperature >= 1415) {
                // 4 FE/tick -> 1415C to 2013C (bituminous coal/lava 1415C)
                int X = 4;
                generator.setEnergyLevel(Math.min(generator.ENERGY_STORAGE.getEnergyStored() + X, maxCap));
                generator.FLUID_TANK.drain(2 * X, IFluidHandler.FluidAction.EXECUTE);
            } else if(generator.temperature >= 1350) {
                // 3 FE/tick -> 1350C to 1414C (charcoal/lignite 1350C)
                int X = 3;
                generator.setEnergyLevel(Math.min(generator.ENERGY_STORAGE.getEnergyStored() + X, maxCap));
                generator.FLUID_TANK.drain(2 * X, IFluidHandler.FluidAction.EXECUTE);
            } else if (generator.temperature >= 1100) {
                // 2 FE/tick -> 1100C to 1349C
                int X = 2;
                generator.setEnergyLevel(Math.min(generator.ENERGY_STORAGE.getEnergyStored() + X, maxCap));
                generator.FLUID_TANK.drain(2 * X, IFluidHandler.FluidAction.EXECUTE);
            } else if(generator.temperature >= 650) {
                // 1 FE/tick -> 650C to 1099C (logs ~650C)
                int X = 1;
                generator.setEnergyLevel(Math.min(generator.ENERGY_STORAGE.getEnergyStored() + X, maxCap));
                generator.FLUID_TANK.drain(2 * X, IFluidHandler.FluidAction.EXECUTE);
            } else if (generator.temperature >= 100) {
                // Min 0.5 FE/tick -> 100C to 649C
                int X = 1;
                if (generator.tick <= 0) {
                    generator.tick += 1;
                    generator.setEnergyLevel(Math.min(generator.ENERGY_STORAGE.getEnergyStored() + X, maxCap));
                    generator.FLUID_TANK.drain(2 * X, IFluidHandler.FluidAction.EXECUTE);
                }
            }

            level.setBlock(pos, state.setValue(steam_generator.STEAM, true), Block.UPDATE_ALL);
            if (generator.tick > 0) {
                generator.tick -= 1;
            }
        }

        if(generator.ENERGY_STORAGE.getEnergyStored() >= generator.ENERGY_STORAGE.getMaxEnergyStored()
                || !hasEnoughFluid(generator) || generator.temperature < 100) {
            level.setBlock(pos, state.setValue(steam_generator.STEAM, false), Block.UPDATE_ALL);
        }

        if(hasFluidItemInSourceSlot(generator)) {
            transferItemFluidToFluidTank(generator);
        }

        //output energy on block sides
        generator.outputEnergy();
        generator.setChanged();
    }

    private static boolean hasEnoughFluid(SteamGeneratorBlockEntity generator) {
        return generator.FLUID_TANK.getFluidAmount() >= 2;
    }

    private static void transferItemFluidToFluidTank(SteamGeneratorBlockEntity generator) {
        generator.inventory.getStackInSlot(0).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int drainAmount = Math.min(generator.FLUID_TANK.getSpace(), 1000);

            FluidStack stack = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            if(generator.FLUID_TANK.isFluidValid(stack)) {
                stack = handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
                fillTankWithFluid(generator, stack, handler.getContainer());
            }
        });
    }

    private static void fillTankWithFluid(SteamGeneratorBlockEntity generator, FluidStack stack, ItemStack container) {
        generator.FLUID_TANK.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        generator.inventory.extractItem(0, 1, false);
        generator.inventory.insertItem(0, container, false);
    }

    private static boolean hasFluidItemInSourceSlot(SteamGeneratorBlockEntity generator) {
        return generator.inventory.getStackInSlot(0).getCount() > 0;
    }


    private final SidedHandler.Noop<IHeatBlock> sidedHeat;
    private final IntArrayBuilder syncableData;
    private float temperature;
    private float targetTemperature;
    private boolean needsRecipeUpdate;
    private int tick;

    /**
     * Prevent the target temperature from "hovering" around a particular value.
     * Effectively means that setTemperature() sets for the next 5 ticks, before it starts to decay naturally.
     */
    private int targetTemperatureStabilityTicks;
    private int lastFillTicks;
    private long lastUpdateTick; // for ICalendarTickable

    public SteamGeneratorBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.STEAM_GENERATOR_BLOCK_ENTITY.get(), pos, state, SteamGeneratorInventory::new, NAME);

        needsRecipeUpdate = true;
        temperature = targetTemperature = 0;
        lastFillTicks = 0;
        lastUpdateTick = Integer.MIN_VALUE;
        tick = 0;


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
        return 1;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        ModMessages.sendToClients(new FluidSyncS2CPacket(this.getFluidStack(), worldPosition));
        return SteamGeneratorContainer.create(this, player.getInventory(), containerId);
    }


    @Override
    public void onLoadAdditional() {
        super.onLoadAdditional();
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
        lazyFluidHandler = LazyOptional.of(() -> FLUID_TANK);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
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
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
        FLUID_TANK.readFromNBT(nbt);
        toggle = nbt.getBoolean("toggle");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putFloat("temperature", temperature);
        nbt.putFloat("targetTemperature", targetTemperature);
        nbt.putInt("targetTemperatureStabilityTicks", targetTemperatureStabilityTicks);
        nbt.putLong("lastUpdateTick", lastUpdateTick);
        nbt.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        nbt = FLUID_TANK.writeToNBT(nbt);
        nbt.putBoolean("toggle", toggle);
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
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        if (cap == HeatCapability.BLOCK_CAPABILITY)
        {
            return sidedHeat.getSidedHandler(side).cast();
        }
        return super.getCapability(cap, side);
    }


    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent() || stack.getItem() instanceof BucketItem;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    public IFluidHandler getFluidStorage() {
        return FLUID_TANK;
    }

    @Override
    public void clearContent() {

    }

    public static class SteamGeneratorInventory implements DelegateItemHandler, INBTSerializable<CompoundTag>, IHeatBlock
    {
        private final SteamGeneratorBlockEntity generator;

        private final InventoryItemHandler inventory;

        SteamGeneratorInventory(InventoryBlockEntity<?> entity)
        {
            generator = (SteamGeneratorBlockEntity) entity;
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
            return generator.temperature;
        }

        @Override
        public void setTemperature(float temperature)
        {
            generator.targetTemperature = temperature;
            generator.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
            generator.markForSync();
        }

        @Override
        public void setTemperatureIfWarmer(float temperature)
        {
            // Override to still cause an update to the stability ticks
            if (temperature >= generator.temperature)
            {
                generator.temperature = temperature;
                generator.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
                generator.markForSync();
            }
        }

    }
    /**
     * Energy stuff
     */

    private static final int maxTransfer = 10;

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(800, maxTransfer) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
        }
        @Override
        public boolean canReceive() {
            return false;
        }
    };

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();


    public IEnergyStorage getEnergyStorage() {
    return ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
    }

    public void outputEnergy() {
        if (this.ENERGY_STORAGE.getEnergyStored() > 0 && this.ENERGY_STORAGE.canExtract() && toggle) {
            for (final var direction : Direction.values()) {
                final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                if (neighbor == null) {
                    continue;
                }
                neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                    if (neighbor != this && storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                        final int canReceive = Math.min(storage.getMaxEnergyStored() - storage.getEnergyStored(), maxTransfer);
                        final int toSend = SteamGeneratorBlockEntity.this.ENERGY_STORAGE.extractEnergy(canReceive,false);
                        final int received = storage.receiveEnergy(toSend, false);

                        SteamGeneratorBlockEntity.this.ENERGY_STORAGE.setEnergy(SteamGeneratorBlockEntity.this.ENERGY_STORAGE.getEnergyStored() + toSend - received);
                    }
                });
            }
        }
    }

    private boolean toggle = false;

    public InteractionResult togglePush(){
        if(!toggle) {
            toggle = true;
        } else {
            toggle = false;
        }
        return InteractionResult.PASS;
    }



    /**
     * Fluid stuff
     */


    private final FluidTank FLUID_TANK = new FluidTank(10000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if(!level.isClientSide()) {
                ModMessages.sendToClients(new FluidSyncS2CPacket(this.fluid, worldPosition));
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }
    };

    public void setFluid(FluidStack stack){
        this.FLUID_TANK.setFluid(stack);
    }

    public FluidStack getFluidStack() {
        return this.FLUID_TANK.getFluid();
    }

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public float render() {
        return (float) FLUID_TANK.getFluidAmount() / (float) FLUID_TANK.getCapacity();
    }

}


