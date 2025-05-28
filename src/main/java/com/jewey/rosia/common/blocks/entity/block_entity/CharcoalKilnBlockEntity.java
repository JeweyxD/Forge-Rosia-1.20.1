package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.charcoal_kiln;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.capabilities.TimerCapability;
import com.jewey.rosia.common.container.CharcoalKilnContainer;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.jewey.rosia.Rosia.MOD_ID;

public class CharcoalKilnBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements ICalendarTickable, MenuProvider
{
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return Helpers.isItem(stack.getItem(), TFCTags.Items.LOG_PILE_LOGS);
    }

    public @NotNull Component getDisplayName() {
        return NAME;
    }

    public static final int SLOT_MIN = 0;
    public static final int SLOT_MAX = 3;

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.charcoal_kiln");


    public static void serverTick(Level level, BlockPos pos, BlockState state, CharcoalKilnBlockEntity kiln)
    {
        kiln.checkForLastTickSync();
        kiln.checkForCalendarUpdate();

        if (kiln.needsRecipeUpdate)
        {
            kiln.needsRecipeUpdate = false;
        }

        if (charcoal_kiln.isValid(level, pos))
        {
            if (state.getValue(charcoal_kiln.LIT)) {
                if (kiln.burnTicks <= 0) {
                    convert(kiln);
                    level.setBlock(pos, state.setValue(charcoal_kiln.LIT, false), 3);
                }
                if (kiln.burnTicks > 0) {
                    kiln.burnTicks -= 1; // Count down
                }
            }
        }
        else if (state.getValue(charcoal_kiln.LIT)) {
            fail(kiln);
            level.setBlock(pos, state.setValue(charcoal_kiln.LIT, false), 3);
        }
    }

    public static void convert(CharcoalKilnBlockEntity kiln) {
        for (int slot = SLOT_MIN; slot <= SLOT_MAX; slot++) {
            final ItemStack inputStack = kiln.inventory.getStackInSlot(slot);
            final int amount = Mth.clamp((int) (kiln.inventory.getStackInSlot(slot).getCount() * 0.75), 1, 8); // Return 75% to charcoal
            final ItemStack charStack = new ItemStack(Items.CHARCOAL, amount);
            if (!inputStack.isEmpty() && Helpers.isItem(inputStack.getItem(), TFCTags.Items.LOG_PILE_LOGS)) {
                kiln.inventory.setStackInSlot(slot, charStack);
            }
        }
    }

    public static void fail(CharcoalKilnBlockEntity kiln) {
        for (int slot = SLOT_MIN; slot <= SLOT_MAX; slot++) {
            final ItemStack inputStack = kiln.inventory.getStackInSlot(slot);
            final int amount = Mth.clamp((int) (kiln.inventory.getStackInSlot(slot).getCount() * 0.5), 1, 8); // Return 50% of logs
            final ItemStack newStack = new ItemStack(kiln.inventory.getStackInSlot(slot).getItem(), amount);
            if (!inputStack.isEmpty() && Helpers.isItem(inputStack.getItem(), TFCTags.Items.LOG_PILE_LOGS)) {
                kiln.inventory.setStackInSlot(slot, newStack);
            }
        }
        kiln.burnTicks = 0;
    }

    public boolean light(BlockState state, CharcoalKilnBlockEntity kiln) {
        if (!state.getValue(charcoal_kiln.LIT))
        {
            for (int slot = SLOT_MIN; slot <= SLOT_MAX; slot++) {
                final ItemStack inputStack = kiln.inventory.getStackInSlot(slot);
                if (!inputStack.isEmpty() && Helpers.isItem(inputStack.getItem(), TFCTags.Items.LOG_PILE_LOGS)) {
                    level.setBlock(worldPosition, state.setValue(charcoal_kiln.LIT, true), 3);
                    kiln.burnTicks = getDuration();
                    litTick = Calendars.get(level).getTicks();
                    return true;
                }
            }
        }
        return false;
    }

    public int getDuration() {
        return (int) (TFCConfig.SERVER.charcoalTicks.get() * 0.75); // Default time 18hr * 75% = 13.5hr
    }



    private boolean needsSlotUpdate = false;
    private int burnTicks; // Ticks remaining on the conversion
    private long litTick;
    private long lastPlayerTick; // Last player tick this forge was ticked (for purposes of catching up)
    private boolean needsRecipeUpdate; // Set to indicate on tick, the cached recipes need to be re-updated

    public CharcoalKilnBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.CHARCOAL_KILN_BLOCK_ENTITY.get(), pos, state, defaultInventory(4), NAME);

        burnTicks = 0;
        lastPlayerTick = Integer.MIN_VALUE;

    }

    @Override
    public void onCalendarUpdate(long ticks)
    {
        assert level != null;
        final BlockState state = level.getBlockState(worldPosition);
        CharcoalKilnBlockEntity kiln = new CharcoalKilnBlockEntity(getBlockPos(), state);
        if (state.getValue(charcoal_kiln.LIT))
        {
            TimerCapability.Remainder remainder =
                    TimerCapability.stepTicks(ticks, burnTicks);

            burnTicks = remainder.burnTicks();
            needsSlotUpdate = true;

            if (remainder.ticks() > 0) {
                convert(kiln);
                state.setValue(charcoal_kiln.LIT, false);
            }
        }
    }

    @Override
    public long getLastCalendarUpdateTick() {
        return lastPlayerTick;
    }

    @Override
    public void setLastCalendarUpdateTick(long l) {
        lastPlayerTick = l;
    }


    public float getBurnTicks()
    {
        return burnTicks;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInv, Player player)
    {
        return CharcoalKilnContainer.create(this, playerInv, windowID);
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        burnTicks = nbt.getInt("burnTicks");
        litTick = nbt.getLong("litTick");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putInt("burnTicks", burnTicks);
        nbt.putLong("litTick", litTick);
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        super.saveAdditional(nbt);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        needsSlotUpdate = true;
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return 8;
    }

    public long getRemainingTicks() {
        return getDuration() - getTicksSinceLit();
    }

    public long getTicksSinceLit()
    {
        assert level != null;
        return Calendars.get(level).getTicks() - litTick;
    }

    @Override
    public void clearContent() {

    }
}