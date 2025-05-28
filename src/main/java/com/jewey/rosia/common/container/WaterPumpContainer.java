package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.WaterPumpBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class WaterPumpContainer extends BlockEntityContainer<WaterPumpBlockEntity>
{
    public final WaterPumpBlockEntity blockEntity;
    private FluidStack fluidStack;


    public static WaterPumpContainer create(WaterPumpBlockEntity pump, Inventory playerInventory, int windowId)
    {
        return new WaterPumpContainer(pump, windowId).init(playerInventory, 20);
    }

    private WaterPumpContainer(WaterPumpBlockEntity pump, int windowId)
    {
        super(ModContainerTypes.WATER_PUMP.get(), windowId, pump);

        addDataSlots(pump.getSyncableData());

        blockEntity = pump;
        this.fluidStack = blockEntity.getFluidStack();
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            int index = 0;
            addSlot(new CallbackSlot(blockEntity, handler, index, 97, 26));
        });
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, WaterPumpBlockEntity.SLOT_FLUID_CONTAINER_IN,
                            WaterPumpBlockEntity.SLOT_FLUID_CONTAINER_IN + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    public void setFluid(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }
    public FluidStack getFluidStack() {
        return fluidStack;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        final Slot slot = slots.get(index);
        if (slot.hasItem()) // Only move an item when the index clicked has any contents
        {
            final ItemStack stack = slot.getItem(); // The item in the current slot
            final ItemStack original = stack.copy(); // The original amount in the slot
            if (moveStack(stack, index)) {return ItemStack.EMPTY;}
            if (stack.getCount() == original.getCount()) {return ItemStack.EMPTY;}
            // Handle updates
            if (stack.isEmpty()) {slot.set(ItemStack.EMPTY);}
            else {slot.setChanged();}
            slot.onTake(player, stack);
            return original;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
