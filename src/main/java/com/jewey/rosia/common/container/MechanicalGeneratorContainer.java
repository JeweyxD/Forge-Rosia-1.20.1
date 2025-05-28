package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.MechanicalGeneratorBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MechanicalGeneratorContainer extends BlockEntityContainer<MechanicalGeneratorBlockEntity> implements ButtonHandlerContainer {

    public static MechanicalGeneratorContainer create(MechanicalGeneratorBlockEntity generator, Inventory playerInventory, int windowId)
    {
        return new MechanicalGeneratorContainer(generator, windowId).init(playerInventory, 0);
    }

    private MechanicalGeneratorContainer(MechanicalGeneratorBlockEntity generator, int windowId)
    {
        super(ModContainerTypes.MECHANICAL_GENERATOR.get(), windowId, generator);

        addDataSlots(generator.getSyncableData());
    }

    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag compoundTag) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (buttonID == 0) { blockEntity.togglePush(); }
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
