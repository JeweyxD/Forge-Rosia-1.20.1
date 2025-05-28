package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.electric_loom;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.ElectricLoomContainer;
import com.jewey.rosia.common.items.ModItems;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.recipe.ElectricLoomRecipe;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.util.IntArrayBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

import java.util.Optional;

import static com.jewey.rosia.Rosia.MOD_ID;


public class ElectricLoomBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements MenuProvider {

    public static final int SLOT_MIN = 0;
    public static final int SLOT_MAX = 2;
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for hopper interaction
            return switch (slot) {
                case 0 -> stack.getItem() == ModItems.STEEL_LOOM_PARTS.get();
                case 1 -> stack.getItem() != ModItems.STEEL_LOOM_PARTS.get();  //Prevent multiple tools being inserted
                case 2 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for player interaction
        return switch (slot) {
            case 0 -> stack.getItem() == ModItems.STEEL_LOOM_PARTS.get();
            case 1 -> stack.getItem() != ModItems.STEEL_LOOM_PARTS.get();  //Prevent multiple tools being inserted
            case 2 -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.electric_loom");
    private int progress = 0;
    private int maxProgress= 200;

    private final IntArrayBuilder syncableData;

    public ElectricLoomBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ELECTRIC_LOOM_BLOCK_ENTITY.get(), pos, state, defaultInventory(3), NAME);
        syncableData = new IntArrayBuilder() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> ElectricLoomBlockEntity.this.progress;
                    case 1 -> ElectricLoomBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> ElectricLoomBlockEntity.this.progress = value;
                    case 1 -> ElectricLoomBlockEntity.this.maxProgress = value;
                }
            }

            public int getCount() {
                return 2;
            }
        };

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(0, 1).extract(2), Direction.Plane.VERTICAL)
                .on(new PartialItemHandler(inventory).insert(0, 1).extract(2), Direction.Plane.HORIZONTAL);
    }

    public ContainerData getSyncableData() {
        return syncableData;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer)
    {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        return ElectricLoomContainer.create(this, pPlayerInventory, pContainerId);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(400, 50) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            if (getLevel() instanceof ServerLevel) {
                ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
            }
        }
    };
    private static final int ENERGY_REQ = 50; // Energy cost to craft item

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
        tag.putInt("electric_loom.progress", progress);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag);
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        progress = nbt.getInt("electric_loom.progress");
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

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, ElectricLoomBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity) && hasEnoughEnergy(pBlockEntity)) {
            pBlockEntity.progress++;
            pLevel.setBlock(pPos, pState.setValue(electric_loom.ON, true), 3);
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.progress > pBlockEntity.maxProgress) {
                craftItem(pBlockEntity);
                extractEnergy(pBlockEntity);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
            pLevel.setBlock(pPos, pState.setValue(electric_loom.ON, false), 3);
        }
    }

    private static void extractEnergy(ElectricLoomBlockEntity pBlockEntity) {
        pBlockEntity.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
    }

    private static boolean hasEnoughEnergy(ElectricLoomBlockEntity pBlockEntity) {
        return pBlockEntity.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ;
    }

    private static boolean hasRecipe(ElectricLoomBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.inventory.getSlots());
        for (int i = 0; i < entity.inventory.getSlots(); i++) {
            inventory.setItem(i, entity.inventory.getStackInSlot(i));
        }

        Optional<ElectricLoomRecipe> match = level.getRecipeManager()
                .getRecipeFor(ElectricLoomRecipe.Type.INSTANCE, inventory, level);

        boolean hasCount = false;
        if (match.isPresent()) {
            int count = match.get().getInputCount();
            hasCount = entity.inventory.getStackInSlot(1).getCount() >= count;
        }

        return match.isPresent() && canInsertAmountIntoOutputSlot(inventory, match.get().getResultItem(null).getCount())
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem(null))
                && hasToolsInToolSlot(entity) && hasCount;
    }


    private static boolean hasToolsInToolSlot(ElectricLoomBlockEntity entity) {
        return entity.inventory.getStackInSlot(0).getItem() == ModItems.STEEL_LOOM_PARTS.get();
    }

    private static void craftItem(ElectricLoomBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.inventory.getSlots());
        for (int i = 0; i < entity.inventory.getSlots(); i++) {
            inventory.setItem(i, entity.inventory.getStackInSlot(i));
        }

        Optional<ElectricLoomRecipe> match = level.getRecipeManager()
                .getRecipeFor(ElectricLoomRecipe.Type.INSTANCE, inventory, level);


        if(match.isPresent()) {
            if(entity.inventory.getStackInSlot(0).hurt(1, RandomSource.create(), null)) {
                entity.inventory.extractItem(0,1, false);
            }
            entity.inventory.extractItem(1, match.get().getInputCount(), false);

            entity.inventory.setStackInSlot(2, new ItemStack(match.get().getResultItem(null).getItem(),
                    entity.inventory.getStackInSlot(2).getCount() + match.get().getResultItem(null).getCount()));

            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory, ItemStack output) {
        return inventory.getItem(2).getItem() == output.getItem() || inventory.getItem(2).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory, int output) {
        return inventory.getItem(2).getMaxStackSize() >= inventory.getItem(2).getCount() + output; //Stack overflow/loss
    }

    @Override
    public void clearContent() {

    }
}
