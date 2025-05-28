package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.lava_basin;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.LavaBasinContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.FluidSyncS2CPacket;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
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
import static net.dries007.tfc.common.capabilities.heat.HeatCapability.adjustTempTowards;
import static net.dries007.tfc.common.capabilities.heat.HeatCapability.targetDeviceTemp;

public class LavaBasinBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements ICalendarTickable
{
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOTS = 1;
    private final float LAVA_TEMP = 1415; //Lava burn temperature to allow pairing with a steam generator at 4 FE/tick
    private final int LAVA_TICKS = 2000; //Lava burn time per 1/10 bucket of lava (100mB)
                                        //This means that 1 bucket equals 20,000 ticks, 80,000 FE
                                        //Coal gives ~8040 FE, ~9.95 coal equals 1 bucket lava
                                        //**Note that this does not account for lower temperature from stone in inventory

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.lava_basin");

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for hopper interaction
           return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent() || stack.getItem() instanceof BucketItem;
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for player interaction
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent() || stack.getItem() instanceof BucketItem;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LavaBasinBlockEntity entity)
    {
        entity.checkForLastTickSync();
        entity.checkForCalendarUpdate();

        if (entity.needsRecipeUpdate)
        {
            entity.needsRecipeUpdate = false;
        }

        //Temperature control
        if (state.getValue(lava_basin.LIT))
        {
            // Update fuel
            if (entity.burnTicks < 0) {
                entity.burnTicks = 1;
            }
            if (entity.burnTicks > 0)
            {
                entity.burnTicks--;
            }
            if (entity.burnTicks == 0 && entity.burnTemperature == 0 && entity.temperature == 0 && !entity.consumeFuel(entity))
            {
                entity.extinguish();
                level.setBlockAndUpdate(pos, state.setValue(lava_basin.LIT, false));
                entity.markForSync();
            }
            // No fuel -> extinguish
            if (entity.burnTemperature > 0 && entity.burnTicks <= 0 && !entity.consumeFuel(entity))
            {
                entity.extinguish();
                if(entity.slag){
                    makeSlag(entity);
                }
            }
        }
        else if (entity.burnTemperature > 0)
        {
            entity.extinguish();
        }

        //Always update temperature until the entity is not hot anymore
        if (entity.temperature > 0 || entity.burnTemperature > 0)
        {
            entity.temperature = adjustDeviceTemp(entity.temperature, entity.burnTemperature);

            HeatCapability.provideHeatTo(level, pos.above(), entity.temperature);

            entity.markForSync();
        }

        //Light if enough lava is in the tank
        if (entity.FLUID_TANK.getFluidAmount() >= 100 && entity.burnTemperature == 0 && entity.burnTicks == 0)
        {
            if (!state.getValue(lava_basin.LIT)) {
                level.setBlockAndUpdate(pos, state.setValue(lava_basin.LIT, true));
                entity.consumeFuel(entity);
                entity.markForSync();
            } else if (entity.temperature > 0) {
                entity.consumeFuel(entity);
                entity.markForSync();
            }
        }

        //Transfer fluid from item in slot 0 to FLUID_TANK
        if (hasFluidItemInSourceSlot(entity)) {
            transferItemFluidToFluidTank(entity);
            entity.markForSync();
        }
    }

    public static float adjustDeviceTemp(float temp, float baseTarget) {
        float target = targetDeviceTemp(baseTarget, 0, false);
        if (temp != target) {
            float deltaPositive = 3.0F;     // Raise temperature 3x faster (LIT)
            float deltaNegative = 3.0F;     // Lower temperature 3x faster (UNLIT)

            return adjustTempTowards(temp, target, deltaPositive, deltaNegative);
        } else {
            return target;
        }
    }

    public static void makeSlag(LavaBasinBlockEntity entity)
    {
        entity.slagLevel++;
        entity.slag = false;
        entity.burnTemperature = getAdjustedTemp(entity);
        setTankCapacity(entity);
    }

    public int getSlagLevel()
    {
        return slagLevel;
    }

    public InteractionResult emptySlag(){
        if(slagLevel != 0) {
            slagLevel = 0;
            this.burnTemperature = getAdjustedTemp(this);
            setTankCapacity(this);
            return InteractionResult.PASS;
        }
        else return InteractionResult.FAIL;
    }

    private static float getAdjustedTemp(LavaBasinBlockEntity entity)
    {
        //Lower temperature when slag in basin, 5° per level; balance for "free" heat source
        return entity.LAVA_TEMP - (entity.slagLevel * 5);
    }

    private boolean consumeFuel(LavaBasinBlockEntity entity)
    {
        if(entity.FLUID_TANK.getFluidAmount() >= 100 && entity.burnTicks == 0)
        {
            if(slag)
            {
                makeSlag(entity);
            }
            slag = true;
            entity.FLUID_TANK.drain(100, IFluidHandler.FluidAction.EXECUTE);
            burnTicks += entity.LAVA_TICKS;
            burnTemperature = getAdjustedTemp(entity);
            markForSync();
        }
        return burnTicks > 0;
    }

    private void extinguish()
    {
        assert level != null;
        burnTicks = 0;
        burnTemperature = 0;
        markForSync();
    }

    private static void transferItemFluidToFluidTank(LavaBasinBlockEntity entity) {
        entity.inventory.getStackInSlot(0).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int drainAmount = Math.min(entity.FLUID_TANK.getSpace(), 1000);

            FluidStack stack = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            //Check if the fluid is valid and is the same as any current fluid in tank
            if(entity.FLUID_TANK.isFluidValid(stack) &&
                    (entity.FLUID_TANK.getFluid().containsFluid(stack)|| entity.FLUID_TANK.getFluid().containsFluid(FluidStack.EMPTY))) {
                stack = handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
                fillTankWithFluid(entity, stack, handler.getContainer());
            }
        });
    }

    private static void fillTankWithFluid(LavaBasinBlockEntity boiler, FluidStack stack, ItemStack container) {
        boiler.FLUID_TANK.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        boiler.inventory.extractItem(0, 1, false);
        boiler.inventory.insertItem(0, container, false);
    }

    private static boolean hasFluidItemInSourceSlot(LavaBasinBlockEntity boiler) {
        return boiler.inventory.getStackInSlot(0).getCount() > 0;
    }


    private final IntArrayBuilder syncableData;
    private boolean needsSlotUpdate = false;
    private float temperature; // Current Temperature
    private int burnTicks; // Ticks remaining on the current item of fuel
    private float burnTemperature; // Temperature provided from the current item of fuel
    private boolean slag; // Indicator for producing slag output
    private int slagLevel; // Amount of slag built up
    private int tankCapacity; // Capacity of the lava tank
    private long lastPlayerTick; // Last player tick this entity was ticked (for purposes of catching up)
    private boolean needsRecipeUpdate; // Set to indicate on tick, the cached recipes need to be re-updated

    public LavaBasinBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.LAVA_BASIN_BLOCK_ENTITY.get(), pos, state, defaultInventory(1), NAME);

        temperature = 0;
        burnTemperature = 0;
        burnTicks = 0;
        slag = false;
        slagLevel = 0;
        tankCapacity = 1000;
        lastPlayerTick = Integer.MIN_VALUE;
        syncableData = new IntArrayBuilder().add(() -> (int) temperature, value -> temperature = value);

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(0).extract(0), Direction.Plane.VERTICAL)
                .on(new PartialItemHandler(inventory).insert(0).extract(0), Direction.Plane.HORIZONTAL);
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
        final BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(lava_basin.LIT))
        {
            HeatCapability.Remainder remainder =
                    consumeFuelForTicks(ticks, burnTicks, burnTemperature, this);

            burnTicks = remainder.burnTicks();
            burnTemperature = remainder.burnTemperature();
            needsSlotUpdate = true;

            if (remainder.ticks() > 0)
            {
                // Consumed all fuel, so extinguish and cool instantly
                extinguish();
            }
        }
    }



    public static HeatCapability.Remainder consumeFuelForTicks(long ticks, int burnTicks, float burnTemperature, LavaBasinBlockEntity entity) {
        //Remove the need for an itemStack for fuel and set fuel time as a constant
        if ((long)burnTicks > ticks)
        {
            burnTicks = (int)((long)burnTicks - ticks);
            return new HeatCapability.Remainder(burnTicks, burnTemperature, 0L);
        } else {
            ticks -= (long)burnTicks;
            burnTicks = 0;

           if(!entity.FLUID_TANK.isEmpty())
            {
                int lavaBurnTicks = entity.LAVA_TICKS;
                if (lavaBurnTicks > ticks) {
                    burnTicks = (int) ((long) lavaBurnTicks - ticks);
                    burnTemperature = lavaBurnTicks;
                    return new HeatCapability.Remainder(burnTicks, burnTemperature, 0L);
                }
                ticks -= (long)lavaBurnTicks;
            }
            return new HeatCapability.Remainder(burnTicks, burnTemperature, ticks);
        }
    }


    @Override
    public int getSlotStackLimit(int slot)
    {
        return switch (slot) {
            case 0 -> 1;
            case 1 -> 32;
            default -> super.getSlotStackLimit(slot);
        };
    }

    @Override
    public long getLastCalendarUpdateTick() {
        return lastPlayerTick;
    }

    @Override
    public void setLastCalendarUpdateTick(long l) {
        lastPlayerTick = l;
    }
    public float getBurnTicks()
    {
        return burnTicks;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        ModMessages.sendToClients(new FluidSyncS2CPacket(this.getFluidStack(), worldPosition));
        return LavaBasinContainer.create(this, player.getInventory(), containerId);
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
        burnTicks = nbt.getInt("burnTicks");
        burnTemperature = nbt.getFloat("burnTemperature");
        slag = nbt.getBoolean("slag");
        slagLevel = nbt.getInt("slagLevel");
        tankCapacity = nbt.getInt("tankCapacity");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        needsRecipeUpdate = true;
        FLUID_TANK.readFromNBT(nbt);
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putFloat("temperature", temperature);
        nbt.putInt("burnTicks", burnTicks);
        nbt.putFloat("burnTemperature", burnTemperature);
        nbt.putBoolean("slag", slag);
        nbt.putInt("slagLevel", slagLevel);
        nbt.putInt("tankCapacity", tankCapacity);
        nbt.putLong("lastPlayerTick", lastPlayerTick);
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


    public IFluidHandler getFluidStorage() {
        return FLUID_TANK;
    }


    /**
     * Fluid stuff
     */


    private final FluidTank FLUID_TANK = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            setTankCapacity(LavaBasinBlockEntity.this);
            if(!level.isClientSide()) {
                ModMessages.sendToClients(new FluidSyncS2CPacket(this.fluid, worldPosition));
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.LAVA;
        }
    };

    private static void setTankCapacity(LavaBasinBlockEntity entity) {
        entity.tankCapacity = 1000 - (entity.slagLevel * 100);
        entity.FLUID_TANK.setCapacity(entity.tankCapacity);
    }

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

    @Override
    public void clearContent() {

    }
}


