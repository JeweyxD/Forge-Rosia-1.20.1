package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.WaterPumpContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.networking.packet.FluidSyncS2CPacket;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.jewey.rosia.Rosia.MOD_ID;

public class WaterPumpBlockEntity extends TickableInventoryBlockEntity<WaterPumpBlockEntity.PumpInventory> implements ICalendarTickable {

    public @NotNull Component getDisplayName() {
        return NAME;
    }

    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOTS = 1;

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.water_pump");

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterPumpBlockEntity pump) {
        pump.checkForLastTickSync();
        pump.checkForCalendarUpdate();

        if (pump.needsRecipeUpdate) {
            pump.needsRecipeUpdate = false;
        }

        // Pump water | only pump if there is enough room in the tank to not waste FE
        if (pump.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ && state.getValue(BlockStateProperties.WATERLOGGED)
                && pump.FLUID_TANK.getFluidAmount() <= pump.FLUID_TANK.getCapacity() - FILL_AMOUNT)
        {
            FluidStack stack = new FluidStack(Fluids.WATER, FILL_AMOUNT);
            pump.FLUID_TANK.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            pump.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
            pump.markForSync();
        }

        if (hasFluidItemInSourceSlot(pump)) {
            transferItemFluidToFluidTank(pump);
            pump.markForSync();
        }
        //Output fluid on block sides if pump has energy (even though the process is free)
        if (pump.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ)
        {
            pump.outputFluid();
            pump.markForSync();
        }
    }

    private static final int ENERGY_REQ = 1; // Energy cost to pump water
    private static final int FILL_AMOUNT = 30; // How much water is pumped per tick

    private static void transferItemFluidToFluidTank(WaterPumpBlockEntity pump) {
        pump.itemHandler.getStackInSlot(0).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int drainAmount = Math.min(pump.FLUID_TANK.getSpace(), 1000);

            FluidStack stack = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            if (pump.FLUID_TANK.isFluidValid(stack)) {
                stack = handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
                fillTankWithFluid(pump, stack, handler.getContainer());
            }
        });
    }

    private static void fillTankWithFluid(WaterPumpBlockEntity pump, FluidStack stack, ItemStack container) {
        pump.FLUID_TANK.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        pump.itemHandler.extractItem(0, 1, false);
        pump.itemHandler.insertItem(0, container, false);
    }

    private static boolean hasFluidItemInSourceSlot(WaterPumpBlockEntity pump) {
        return pump.itemHandler.getStackInSlot(0).getCount() > 0;
    }

    private final IntArrayBuilder syncableData;
    private boolean needsRecipeUpdate;
    private float ticks;
    private long lastUpdateTick; // for ICalendarTickable

    public WaterPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_PUMP_BLOCK_ENTITY.get(), pos, state, PumpInventory::new, NAME);

        needsRecipeUpdate = true;
        lastUpdateTick = Integer.MIN_VALUE;
        ticks = 0;

        syncableData = new IntArrayBuilder();
    }

    public ContainerData getSyncableData() {
        return syncableData;
    }


    @Override
    public void onCalendarUpdate(long ticks) {
        assert level != null;

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
    public int getSlotStackLimit(int slot) {
        return 1;
    }


    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        ModMessages.sendToClients(new FluidSyncS2CPacket(this.getFluidStack(), worldPosition));
        return WaterPumpContainer.create(this, player.getInventory(), containerId);
    }


    @Override
    public void onLoadAdditional() {
        super.onLoadAdditional();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
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
    public void loadAdditional(CompoundTag nbt) {
        lastUpdateTick = nbt.getLong("lastUpdateTick");
        needsRecipeUpdate = true;
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
        ticks = nbt.getFloat("ticks");
        FLUID_TANK.readFromNBT(nbt);
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putLong("lastUpdateTick", lastUpdateTick);
        nbt.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        nbt.putFloat("ticks", ticks);
        nbt = FLUID_TANK.writeToNBT(nbt);
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
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return lazyItemHandler.cast();
            }
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
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

    public static class PumpInventory implements DelegateItemHandler, INBTSerializable<CompoundTag> {
        private final WaterPumpBlockEntity pump;

        private final InventoryItemHandler inventory;

        PumpInventory(InventoryBlockEntity<?> entity) {
            pump = (WaterPumpBlockEntity) entity;
            inventory = new InventoryItemHandler(entity, SLOTS);
        }

        @Override
        public IItemHandlerModifiable getItemHandler() {
            return inventory;
        }

        @Override
        public CompoundTag serializeNBT() {
            final CompoundTag nbt = new CompoundTag();
            nbt.put("inventory", inventory.serializeNBT());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }
    }

    public void outputFluid() {
        if (this.FLUID_TANK.getFluidAmount() >= 0) {
            for (final var direction : Direction.values()) {
                final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                if (neighbor == null) {
                    continue;
                }

                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(storage -> {
                    if (neighbor != this && storage.getFluidInTank(1).getAmount() < storage.getTankCapacity(1)) {
                        final int canReceive = Math.min(storage.getTankCapacity(1) - storage.getFluidInTank(1).getAmount(), FILL_AMOUNT);
                        FluidStack toSendFluid = WaterPumpBlockEntity.this.FLUID_TANK.drain(canReceive, IFluidHandler.FluidAction.EXECUTE);
                        storage.fill(toSendFluid, IFluidHandler.FluidAction.EXECUTE);

                        WaterPumpBlockEntity.this.FLUID_TANK.setFluid(WaterPumpBlockEntity.this.FLUID_TANK.getFluidInTank(1));
                    }
                });
            }
        }
    }

    /**
     * Energy stuff
     */


    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(800, 10) {
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

    /**
     * Fluid stuff
     */


    private final FluidTank FLUID_TANK = new FluidTank(10000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (!level.isClientSide()) {
                ModMessages.sendToClients(new FluidSyncS2CPacket(this.fluid, worldPosition));
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }
    };

    public void setFluid(FluidStack stack) {
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
