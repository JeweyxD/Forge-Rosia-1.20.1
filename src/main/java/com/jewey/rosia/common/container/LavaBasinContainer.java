package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.LavaBasinBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class LavaBasinContainer extends BlockEntityContainer<LavaBasinBlockEntity> implements ButtonHandlerContainer
{
    public final LavaBasinBlockEntity blockEntity;
    private FluidStack fluidStack;


    public static LavaBasinContainer create(LavaBasinBlockEntity boiler, Inventory playerInventory, int windowId)
    {
        return new LavaBasinContainer(boiler, windowId).init(playerInventory, 20);
    }

    private LavaBasinContainer(LavaBasinBlockEntity forge, int windowId)
    {
        super(ModContainerTypes.LAVA_BASIN.get(), windowId, forge);

        addDataSlots(forge.getSyncableData());

        blockEntity = forge;
        this.fluidStack = blockEntity.getFluidStack();
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            int index = 0;
            addSlot(new CallbackSlot(blockEntity, handler, index, 98, 26)); //Fluid in
        });
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, LavaBasinBlockEntity.SLOT_FLUID_CONTAINER_IN,
                            LavaBasinBlockEntity.SLOT_FLUID_CONTAINER_IN + 1, false);
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
    public void onButtonPress(int buttonID, @Nullable CompoundTag compoundTag) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (buttonID == 0) { blockEntity.emptySlag(); }
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
