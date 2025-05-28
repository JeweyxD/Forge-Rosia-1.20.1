package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.ElectricForgeBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;


public class ElectricForgeContainer extends BlockEntityContainer<ElectricForgeBlockEntity> implements ButtonHandlerContainer
{

    public static ElectricForgeContainer create(ElectricForgeBlockEntity forge, Inventory playerInventory, int windowId)
    {
        return new ElectricForgeContainer(forge, windowId).init(playerInventory, 20);
    }

    private ElectricForgeContainer(ElectricForgeBlockEntity forge, int windowId)
    {
        super(ModContainerTypes.ELECTRIC_FORGE.get(), windowId, forge);

        addDataSlots(forge.getSyncableData());
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, ElectricForgeBlockEntity.SLOT_EXTRA_MIN,
                            ElectricForgeBlockEntity.SLOT_EXTRA_MAX + 1, false)
                            && !moveItemStackTo(stack, ElectricForgeBlockEntity.SLOT_INPUT_MIN,
                            ElectricForgeBlockEntity.SLOT_INPUT_MAX + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            // Input slots
            // Note: the order of these statements is important
            int index = ElectricForgeBlockEntity.SLOT_INPUT_MIN;
            addSlot(new CallbackSlot(blockEntity, handler, index++, 44, 19));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 62, 19));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 80, 19));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 98, 19));
            addSlot(new CallbackSlot(blockEntity, handler, index, 116, 19));

            // Extra slots (for ceramic molds)
            int i = ElectricForgeBlockEntity.SLOT_EXTRA_MIN;
            {
                addSlot(new CallbackSlot(blockEntity, handler, i, 80, 39));
            }
        });
    }

    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag compoundTag) {
        if (player instanceof ServerPlayer serverPlayer)
        {
            if(!Screen.hasShiftDown()){
                if (buttonID == 0) {
                    blockEntity.upTemp(serverPlayer, 10);
                }
                if (buttonID == 1) {
                    blockEntity.downTemp(serverPlayer, 10);
                }
            }
            else if(Screen.hasShiftDown()){
                if (buttonID == 0) {
                    blockEntity.upTemp(serverPlayer, 100);
                }
                if (buttonID == 1) {
                    blockEntity.downTemp(serverPlayer, 100);
                }
            }
            if(buttonID == 2){ blockEntity.setTemp0(serverPlayer, 0); }
        }
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