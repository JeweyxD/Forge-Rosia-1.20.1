package com.jewey.rosia.common.container;

import com.jewey.rosia.common.items.ProspectingKitItem;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.ItemStackContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ProspectingKitContainer extends ItemStackContainer  implements ButtonHandlerContainer {
    public ItemStack useStack;
    public int[] grid;
    public Item[] ore;
    public ProspectingKitContainer(ItemStack stack, InteractionHand hand, int slot, Inventory playerInv, int windowId) {
        super(ModContainerTypes.PROSPECTING_KIT.get(), windowId, playerInv, stack, hand, slot);
        if (stack.getItem() instanceof ProspectingKitItem)
        {
            useStack = stack;
            grid = ((ProspectingKitItem) stack.getItem()).grid.clone();
            ore = ((ProspectingKitItem) stack.getItem()).ore.clone();
        }
    }

    public static ProspectingKitContainer create(ItemStack stack, InteractionHand hand, int slot, Inventory playerInv, int windowId)
    {
        return new ProspectingKitContainer(stack, hand, slot, playerInv, windowId).init(playerInv, 29);
    }

    public int[] getGrid()
    {
        return grid;
    }

    public Item[] getOre()
    {
        return ore;
    }

    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag compoundTag) {
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
