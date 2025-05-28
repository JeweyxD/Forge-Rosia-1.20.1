package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.canning_press;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.WrappedHandlerEnergy;
import com.jewey.rosia.common.capabilities.food.RosiaFoodTraits;
import com.jewey.rosia.common.container.CanningPressContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.util.ModEnergyStorage;
import com.jewey.rosia.util.RosiaTags;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.util.IntArrayBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.jewey.rosia.Rosia.MOD_ID;


public class CanningPressBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements MenuProvider {

    public static final int SLOT_MIN = 0;
    public static final int SLOT_MAX = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for hopper interaction
            return switch (slot) {
                case 0 -> stack.is(RosiaTags.Items.CANNED_ITEM);
                case 1 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for player interaction
        return switch (slot) {
            case 0 -> stack.is(RosiaTags.Items.CANNED_ITEM);
            case 1 -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.canning_press");
    private int progress = 0;
    private int maxProgress= 50;

    private final IntArrayBuilder syncableData;

    public CanningPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CANNING_PRESS_BLOCK_ENTITY.get(), pos, state, defaultInventory(3), NAME);
        syncableData = new IntArrayBuilder() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> CanningPressBlockEntity.this.progress;
                    case 1 -> CanningPressBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CanningPressBlockEntity.this.progress = value;
                    case 1 -> CanningPressBlockEntity.this.maxProgress = value;
                }
            }

            public int getCount() {
                return 2;
            }
        };

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(0).extract(1), Direction.DOWN)
                .on(new PartialItemHandler(inventory).insert(0).extract(1), Direction.NORTH);
    }

    public ContainerData getSyncableData() {
        return syncableData;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer)
    {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        return CanningPressContainer.create(this, pPlayerInventory, pContainerId);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(100, 10) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            if (getLevel() instanceof ServerLevel) {
                ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
            }
        }
    };
    private static final int ENERGY_REQ = 10; // Energy cost to craft item

    private final Map<Direction, LazyOptional<WrappedHandlerEnergy>> directionWrappedHandlerMapEnergy =
            //Handler for sided energy: extract, receive, canExtract, canReceive (seems redundant, but it breaks otherwise because why not)
            //Determines what sides can perform actions
            Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> true, (i) -> true, true, true)),
                    Direction.UP, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE,(i) -> false, (i) -> false, false, false)),
                    Direction.NORTH, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> true, (i) -> true, true, true)),
                    Direction.SOUTH, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
                    Direction.EAST, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
                    Direction.WEST, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)));

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();


    public IEnergyStorage getEnergyStorage() {
        return ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
    }



    @Override
    public void onLoadAdditional() {
        super.onLoadAdditional();
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        tag.putInt("canning_press.progress", progress);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag);
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        progress = nbt.getInt("canning_press.progress");
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
        super.loadAdditional(nbt);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, CanningPressBlockEntity pBlockEntity) {
        if(!pBlockEntity.inventory.getStackInSlot(0).isEmpty() && hasEnoughEnergy(pBlockEntity) && canOutput(pBlockEntity)) {
            pBlockEntity.progress++;
            pLevel.setBlock(pPos, pState.setValue(canning_press.ON, true), 3);
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.progress > pBlockEntity.maxProgress) {
                craftItem(pBlockEntity);
                extractEnergy(pBlockEntity);
            }

            if(pBlockEntity.progress == pBlockEntity.maxProgress) {
                pLevel.playSound(null, pPos, SoundEvents.NETHERITE_BLOCK_PLACE,
                        SoundSource.BLOCKS, 1F, 1);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
            pLevel.setBlock(pPos, pState.setValue(canning_press.ON, false), 3);
        }
    }

    private static void extractEnergy(CanningPressBlockEntity pBlockEntity) {
        pBlockEntity.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
    }

    private static boolean hasEnoughEnergy(CanningPressBlockEntity pBlockEntity) {
        return pBlockEntity.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ;
    }

    private static void craftItem(CanningPressBlockEntity entity) {
        ItemStack inputStack = entity.inventory.getStackInSlot(0).copy();  // Keep as an itemStack to avoid losing nutrient/nbt data
        inputStack.setCount(entity.inventory.getStackInSlot(1).getCount() + 1);
        entity.inventory.setStackInSlot(1, inputStack);

        ItemStack outputStack = entity.inventory.getStackInSlot(1);
        FoodCapability.applyTrait(outputStack, RosiaFoodTraits.SEALED);

        entity.inventory.extractItem(0,1, false);

        entity.resetProgress();
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private static boolean canOutput(CanningPressBlockEntity entity) {
        ItemStackHandler inventory = entity.inventory;
        boolean canInsertAmountIntoOutputSlot = inventory.getStackInSlot(1).getMaxStackSize() > inventory.getStackInSlot(1).getCount();
        boolean canInsertItemIntoOutputSlot = inventory.getStackInSlot(0).getItem() == inventory.getStackInSlot(1).getItem()
                || inventory.getStackInSlot(1).isEmpty();
        return canInsertAmountIntoOutputSlot && canInsertItemIntoOutputSlot;
    }

    public ItemStack getRenderStack() {
        ItemStack stack;
        if(!inventory.getStackInSlot(0).isEmpty()) {
            stack = inventory.getStackInSlot(0);
        } else {
            stack = inventory.getStackInSlot(1);
        }

        return stack;
    }

    public void setHandler(ItemStackHandler itemStackHandler) {
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setStackInSlot(i, itemStackHandler.getStackInSlot(i));
        }
    }

    @Override
    public void clearContent() {

    }
}
