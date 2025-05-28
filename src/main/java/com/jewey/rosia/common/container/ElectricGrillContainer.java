package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.ElectricGrillBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class ElectricGrillContainer extends BlockEntityContainer<ElectricGrillBlockEntity>
{

    public static ElectricGrillContainer create(ElectricGrillBlockEntity grill, Inventory playerInventory, int windowId)
    {
        return new ElectricGrillContainer(grill, windowId).init(playerInventory, 20);
    }

    private ElectricGrillContainer(ElectricGrillBlockEntity grill, int windowId)
    {
        super(ModContainerTypes.ELECTRIC_GRILL.get(), windowId, grill);

        addDataSlots(grill.getSyncableData());
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, ElectricGrillBlockEntity.SLOT_INPUT_MIN,
                            ElectricGrillBlockEntity.SLOT_INPUT_MAX + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            // Input slots
            // Note: the order of these statements is important
            int index = ElectricGrillBlockEntity.SLOT_INPUT_MIN;
            addSlot(new CallbackSlot(blockEntity, handler, index++, 44, 20));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 62, 20));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 80, 20));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 98, 20));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 116, 20));

            addSlot(new CallbackSlot(blockEntity, handler, index++, 44, 38));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 62, 38));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 80, 38));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 98, 38));
            addSlot(new CallbackSlot(blockEntity, handler, index, 116, 38));
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