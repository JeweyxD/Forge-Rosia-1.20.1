package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.CoolingBasinContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.FluidSyncS2CPacket;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.jewey.rosia.Rosia.MOD_ID;

public class CoolingBasinBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> {
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOTS = 10;
    public int soundCooldownTicks = 0;

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.cooling_basin");

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoolingBasinBlockEntity basin) {
        basin.checkForLastTickSync();

        if(basin.FLUID_TANK.getFluidAmount() > 1)
        {
            for(int i = 1; i <= 9; i++)
            {
                ItemStack stack = basin.inventory.getStackInSlot(i);
                stack.getCapability(HeatCapability.CAPABILITY).ifPresent(cap -> {
                    // Update temperature of item
                    float itemTemp = cap.getTemperature();
                    if(itemTemp > 0)
                    {
                        cap.setTemperature(Math.max(itemTemp - 5, 0)); // Don't go below 0
                        basin.FLUID_TANK.drain(1, IFluidHandler.FluidAction.EXECUTE);
                        if(basin.soundCooldownTicks == 0)
                        {
                            Helpers.playSound(level, basin.getBlockPos(), SoundEvents.FIRE_EXTINGUISH);
                            basin.soundCooldownTicks = 10;
                            if(level instanceof ServerLevel server)
                            {
                                final double x = pos.getX() + 0.5;
                                final double y = pos.getY();
                                final double z = pos.getZ() + 0.5;
                                final RandomSource random = level.getRandom();
                                server.sendParticles(TFCParticles.BUBBLE.get(), x + random.nextFloat() * 0.375 - 0.1875, y + 15f / 16f, z + random.nextFloat() * 0.375 - 0.1875, 6, 0, 0, 0, 1);
                                server.sendParticles(TFCParticles.STEAM.get(), x + random.nextFloat() * 0.375 - 0.1875, y + 15f / 16f, z + random.nextFloat() * 0.375 - 0.1875, 6, 0, 0, 0, 1);
                            }
                        }
                    }
                });
            }
        }

        if(basin.soundCooldownTicks > 0)
        {
            basin.soundCooldownTicks--;
        }

        //Transfer fluid from item in slot 0 to FLUID_TANK
        if(hasFluidItemInSourceSlot(basin)) {
            transferItemFluidToFluidTank(basin);
        }
    }

    private static void transferItemFluidToFluidTank(CoolingBasinBlockEntity basin) {
        basin.inventory.getStackInSlot(0).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int drainAmount = Math.min(basin.FLUID_TANK.getSpace(), 1000);

            FluidStack stack = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            //Check if the fluid is valid and is the same as any current fluid in tank
            if(basin.FLUID_TANK.isFluidValid(stack)
                    && (basin.FLUID_TANK.getFluid().containsFluid(stack) || basin.FLUID_TANK.getFluid().containsFluid(FluidStack.EMPTY))) {
                stack = handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
                fillTankWithFluid(basin, stack, handler.getContainer());
            }
        });
    }

    private static void fillTankWithFluid(CoolingBasinBlockEntity basin, FluidStack stack, ItemStack container) {
        basin.FLUID_TANK.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        basin.inventory.extractItem(0, 1, false);
        basin.inventory.insertItem(0, container, false);
    }

    private static boolean hasFluidItemInSourceSlot(CoolingBasinBlockEntity basin) {
        return basin.inventory.getStackInSlot(0).getCount() > 0;
    }

    public CoolingBasinBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.COOLING_BASIN_BLOCK_ENTITY.get(), pos, state, defaultInventory(10), NAME);
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
        return CoolingBasinContainer.create(this, player.getInventory(), containerId);
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
        FLUID_TANK.readFromNBT(nbt);
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
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
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(10) {
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
            case 0 -> Helpers.mightHaveCapability(stack, Capabilities.FLUID_ITEM);
            default -> super.isItemValid(slot, stack);
        };
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

    public IFluidHandler getFluidStorage() {
        return FLUID_TANK;
    }

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public float render() {
        return (float) FLUID_TANK.getFluidAmount() / (float) FLUID_TANK.getCapacity();
    }
}
