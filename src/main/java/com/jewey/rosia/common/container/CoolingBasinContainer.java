package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.CoolingBasinBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CoolingBasinContainer extends BlockEntityContainer<CoolingBasinBlockEntity>
{
    public final CoolingBasinBlockEntity blockEntity;
    private FluidStack fluidStack;


    public static CoolingBasinContainer create(CoolingBasinBlockEntity basin, Inventory playerInventory, int windowId)
    {
        return new CoolingBasinContainer(basin, windowId).init(playerInventory, 20);
    }

    private CoolingBasinContainer(CoolingBasinBlockEntity basin, int windowId)
    {
        super(ModContainerTypes.COOLING_BASIN.get(), windowId, basin);

        blockEntity = basin;
        this.fluidStack = blockEntity.getFluidStack();
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            addSlot(new CallbackSlot(blockEntity, handler, 0, 42, 26)); //Fluid in
            addSlot(new CallbackSlot(blockEntity, handler, 1, 80, 26)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 2, 98, 26)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 3, 116, 26)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 4, 80, 44)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 5, 98, 44)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 6, 116, 44)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 7, 80, 62)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 8, 98, 62)); //Slot
            addSlot(new CallbackSlot(blockEntity, handler, 9, 116, 62)); //Slot
        });
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, CoolingBasinBlockEntity.SLOT_FLUID_CONTAINER_IN,
                            9 + 1, false);
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
