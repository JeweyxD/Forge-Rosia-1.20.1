package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.ScrapingMachineBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class ScrapingMachineContainer extends BlockEntityContainer<ScrapingMachineBlockEntity>
{
    public static ScrapingMachineContainer create(ScrapingMachineBlockEntity entity, Inventory playerInventory, int windowId)
    {
        return new ScrapingMachineContainer(entity, windowId).init(playerInventory, 0);
    }


    private ScrapingMachineContainer(ScrapingMachineBlockEntity entity, int windowId)
    {
        super(ModContainerTypes.SCRAPING_MACHINE.get(), windowId, entity);
        addDataSlots(entity.getSyncableData());
    }

    public boolean isCrafting() {
        return blockEntity.getSyncableData().get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.blockEntity.getSyncableData().get(0);
        int maxProgress = this.blockEntity.getSyncableData().get(1);
        int progressArrowSize = 24; // This is the height or width in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, ScrapingMachineBlockEntity.SLOT_MIN,
                            ScrapingMachineBlockEntity.SLOT_MAX + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            addSlot(new CallbackSlot(blockEntity, handler, 0, 26, 47));  //Tool
            addSlot(new CallbackSlot(blockEntity, handler, 1, 62, 47));  //Input
            addSlot(new CallbackSlot(blockEntity, handler, 2, 116, 47)); //Output
        });
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