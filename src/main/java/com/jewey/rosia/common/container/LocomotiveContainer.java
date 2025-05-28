package com.jewey.rosia.common.container;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class LocomotiveContainer extends net.dries007.tfc.common.container.Container
{
    private final Container container;
    public LocomotiveContainer(MenuType<?> type, int windowId, Inventory inv, int rows)
    {
        this(type, windowId, inv, new SimpleContainer(12), rows);
    }

    public static LocomotiveContainer createMenu(int windowId, Inventory inv, FriendlyByteBuf data)
    {
       return new LocomotiveContainer(ModContainerTypes.LOCOMOTIVE.get(), windowId, inv, 1);
    }

    public static LocomotiveContainer createMenu(int windowId, Inventory inv, Container container)
    {
       return new LocomotiveContainer(ModContainerTypes.LOCOMOTIVE.get(), windowId, inv, container, 1);
    }

    public LocomotiveContainer(MenuType<?> type, int windowId, Inventory inv, Container container, int rows)
    {
        super(type, windowId);
        checkContainerSize(container, 12);
        this.container = container;

        // Container
        // Inv Slots
        addSlot(new InvSlot(container, 0, 8, 48));
        addSlot(new InvSlot(container, 1, 26, 48));
        addSlot(new InvSlot(container, 2, 44, 48));
        addSlot(new InvSlot(container, 3, 62, 48));
        addSlot(new InvSlot(container, 4, 80, 48));
        addSlot(new InvSlot(container, 5, 98, 48));
        addSlot(new InvSlot(container, 6, 116, 48));
        addSlot(new InvSlot(container, 7, 134, 48));
        addSlot(new InvSlot(container, 8, 152, 48));

        // Fuel Slots
        addSlot(new FuelSlot(container, 9, 80, 25)); //Center
        addSlot(new FuelSlot(container, 10, 62, 22)); //Left
        addSlot(new FuelSlot(container, 11, 98, 22)); //Right

        // Inv & Hotbar
        final int yOffset = -19;
        for (int row = 0; row < 3; ++row)
        {
            for (int col = 0; col < 9; ++col)
            {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + yOffset));
            }
        }

        for (int col = 0; col < 9; ++col)
        {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 161 + yOffset));
        }
    }

    private static class InvSlot extends Slot
    {
        public InvSlot(Container container, int pSlot, int pX, int pY)
        {
            super(container, pSlot, pX, pY);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return super.mayPlace(stack) && TFCChestBlockEntity.isValid(stack);
        }
    }

    private static class FuelSlot extends Slot
    {
        public FuelSlot(Container container, int pSlot, int pX, int pY)
        {
            super(container, pSlot, pX, pY);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return super.mayPlace(stack) && stack.is(TFCTags.Items.FORGE_FUEL);
        }

        @Override
        public int getMaxStackSize()
        {
            return 4;
        }
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex) {
        boolean var10000;
        switch (this.typeOf(slotIndex)) {
            case CONTAINER:
                var10000 = true;
                break;
            case HOTBAR:
                var10000 = !moveItemStackTo(stack, 0, 12 + 27, false);
                break;
            case MAIN_INVENTORY:
                var10000 = !moveItemStackTo(stack, 12 + 27, 12 + 36, false);
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return var10000;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.container.stillValid(pPlayer);
    }
}