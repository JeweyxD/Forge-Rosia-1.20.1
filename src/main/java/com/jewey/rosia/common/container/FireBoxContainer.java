package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.FireBoxBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.jewey.rosia.common.blocks.entity.block_entity.FireBoxBlockEntity.SLOT_FUEL_MIN;

public class FireBoxContainer extends BlockEntityContainer<FireBoxBlockEntity>
{
    public static FireBoxContainer create(FireBoxBlockEntity forge, Inventory playerInventory, int windowId)
    {
        return new FireBoxContainer(forge, windowId).init(playerInventory, 20);
    }

    private FireBoxContainer(FireBoxBlockEntity forge, int windowId)
    {
        super(ModContainerTypes.FIRE_BOX.get(), windowId, forge);

        addDataSlots(forge.getSyncableData());
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, SLOT_FUEL_MIN,
                            FireBoxBlockEntity.SLOT_FUEL_MAX + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            // Fuel slots
            int index = SLOT_FUEL_MIN;
            addSlot(new CallbackSlot(blockEntity, handler, index++, 80, 60));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 62, 57));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 98, 57));
            //output for wood ash
            addSlot(new CallbackSlot(blockEntity, handler, index, 125, 76));

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