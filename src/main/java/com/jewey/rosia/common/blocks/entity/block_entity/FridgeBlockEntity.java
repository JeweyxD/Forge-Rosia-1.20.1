package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.fridge;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.MultiblockBlockEntity;
import com.jewey.rosia.common.capabilities.MultiblockCapability;
import com.jewey.rosia.common.capabilities.food.RosiaFoodTraits;
import com.jewey.rosia.common.container.FridgeContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.util.ModEnergyStorage;
import com.jewey.rosia.util.RosiaTags;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

import static com.jewey.rosia.Rosia.MOD_ID;

public class FridgeBlockEntity extends MultiblockBlockEntity implements MenuProvider, DelegateItemHandler, ISlotCallback {

    public static final int SLOT_MIN = 0;
    public static final int SLOT_MAX = 8;

    @Override
    public FridgeBlockEntity master() {
        if (isDummy) {
            assert level != null;
            BlockEntity blockEntity = level.getBlockEntity(getBlockPos().below());
            return (blockEntity instanceof FridgeBlockEntity fridge) ? fridge : null;
        } else {
            return this;
        }
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.fridge");

    public static void serverTick(Level level, BlockPos pos, BlockState state, FridgeBlockEntity Fridge) {
        Fridge.checkForLastTickSync();

        if (Fridge.needsRecipeUpdate) {
            Fridge.needsRecipeUpdate = false;
        }

        // If item in slots -> turn on and consume power
        if(Fridge.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ)
        {
            for (int slot = SLOT_MIN; slot <= SLOT_MAX; slot++)
            {
                final ItemStack inputStack = Fridge.inventory.getStackInSlot(slot);
                if (!inputStack.isEmpty()
                        && Helpers.mightHaveCapability(inputStack, FoodCapability.CAPABILITY))
                {
                    level.setBlock(pos, state.setValue(fridge.ON, true), 3);
                    break;  // End the loop, so it doesn't find other empty slots and turn off
                }
                else
                {
                    level.setBlock(pos, state.setValue(fridge.ON, false), 3);
                }
            }
            for (int slot = SLOT_MIN; slot <= SLOT_MAX; slot++)
            {
                final ItemStack inputStack = Fridge.inventory.getStackInSlot(slot);
                if (state.getValue(fridge.ON))
                {
                    FoodCapability.applyTrait(inputStack, RosiaFoodTraits.REFRIGERATED);
                }
            }
        }
        else
        {
            level.setBlock(pos, state.setValue(fridge.ON, false), 3);
            for (int slot = SLOT_MIN; slot <= SLOT_MAX; slot++)
            {
                final ItemStack inputStack = Fridge.inventory.getStackInSlot(slot);
                FoodCapability.removeTrait(inputStack, RosiaFoodTraits.REFRIGERATED);
            }

        }
        if (state.getValue(fridge.ON))
        {
            Fridge.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
        }
    }

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(400, 10) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
        }
    };

    private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
            this, be -> be.energyCap, FridgeBlockEntity::master, registerEnergyStorage(ENERGY_STORAGE)
    );

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.getAndCast();
        }
        return super.getCapability(cap, side);
    }

    private static final int ENERGY_REQ = 1;

    public IEnergyStorage getEnergyStorage() {
        return ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
    }

    private final HeatingRecipe[] cachedRecipes;
    private boolean needsSlotUpdate = false;
    private long lastPlayerTick; // Last player tick this forge was ticked (for purposes of catching up)
    private boolean needsRecipeUpdate; // Set to indicate on tick, the cached recipes need to be re-updated

    public FridgeBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FRIDGE_BLOCK_ENTITY.get(), pos, state, defaultInventory(10), NAME);

        cachedRecipes = new HeatingRecipe[10];
        lastPlayerTick = Integer.MIN_VALUE;

        Arrays.fill(cachedRecipes, null);
    }

    @Deprecated
    public long getLastUpdateTick()
    {
        return lastPlayerTick;
    }

    @Deprecated
    public void setLastUpdateTick(long tick)
    {
        lastPlayerTick = tick;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInv, Player player)
    {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        return FridgeContainer.create(this, playerInv, windowID);
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        nbt.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void ejectInventory()
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            FoodCapability.removeTrait(inventory.getStackInSlot(slot), RosiaFoodTraits.REFRIGERATED);
        }
        super.ejectInventory();
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        needsSlotUpdate = true;
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return 32;
    }

    @Override
    public IItemHandlerModifiable getItemHandler() {
        return inventory;
    }

    @Override
    public void onSlotTake(Player player, int slot, ItemStack stack)
    {
        FoodCapability.removeTrait(stack, RosiaFoodTraits.REFRIGERATED);
    }

    @Override
    public void onCarried(ItemStack stack)
    {
        FoodCapability.removeTrait(stack, RosiaFoodTraits.REFRIGERATED);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        FoodCapability.applyTrait(stack, RosiaFoodTraits.REFRIGERATED);
        inventory.setStackInSlot(slot, stack);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        FoodCapability.applyTrait(stack, RosiaFoodTraits.REFRIGERATED);
        final ItemStack result = inventory.insertItem(slot, stack, simulate);
        if (simulate)
        {
            FoodCapability.removeTrait(result, RosiaFoodTraits.REFRIGERATED); // Un-do preservation for simulated actions
        }
        return result;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        final ItemStack result = inventory.extractItem(slot, amount, simulate);
        FoodCapability.removeTrait(result, RosiaFoodTraits.REFRIGERATED);
        return result;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return ItemSizeManager.get(stack).getSize(stack).isSmallerThan(Size.LARGE)
                && Helpers.mightHaveCapability(stack, FoodCapability.CAPABILITY)
                && !stack.is(RosiaTags.Items.CANNED_ITEM) && super.isItemValid(slot, stack);
    }

    @Override
    public void clearContent() {

    }
}