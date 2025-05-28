package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.auto_quern;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.AutoQuernContainer;
import com.jewey.rosia.common.items.ModItems;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.recipe.AutoQuernRecipe;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.recipes.QuernRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.util.IntArrayBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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


public class AutoQuernBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements MenuProvider
{
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
                case 0 -> stack.getItem() == ModItems.STEEL_GRINDSTONE.get();
                case 1 -> stack.getItem() != ModItems.STEEL_GRINDSTONE.get();  //Prevent multiple tools being inserted
                case 2 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for player interaction
        return switch (slot) {
            case 0 -> stack.getItem() == ModItems.STEEL_GRINDSTONE.get();
            case 1 -> stack.getItem() != ModItems.STEEL_GRINDSTONE.get();  //Prevent multiple tools being inserted
            case 2 -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return  NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.auto_quern");

    public int progress = 0;
    public int maxProgress= 120;
    private float sTick;

    private final IntArrayBuilder syncableData;

    public AutoQuernBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.AUTO_QUERN_BLOCK_ENTITY.get(), pos, state, defaultInventory(3), NAME);
        syncableData = new IntArrayBuilder() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> AutoQuernBlockEntity.this.progress;
                    case 1 -> AutoQuernBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AutoQuernBlockEntity.this.progress = value;
                    case 1 -> AutoQuernBlockEntity.this.maxProgress = value;
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
        return AutoQuernContainer.create(this, pPlayerInventory, pContainerId);
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
    public void saveAdditional(CompoundTag tag) {
        tag.putInt("auto_quern.progress", progress);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag);
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        progress = nbt.getInt("auto_quern.progress");
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

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, AutoQuernBlockEntity pBlockEntity) {
        if(pBlockEntity.sTick > 0) { pBlockEntity.sTick -= 1; }
        if(hasRecipe(pBlockEntity) && hasEnoughEnergy(pBlockEntity)) {
            pBlockEntity.progress++;
            pLevel.setBlock(pPos, pState.setValue(auto_quern.ON, true), 3);
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.progress > pBlockEntity.maxProgress) {
                craftItem(pBlockEntity);
                extractEnergy(pBlockEntity);
                pBlockEntity.resetSTick();
            }
            // to prevent sound spamming
            if(pBlockEntity.progress == 15 && pBlockEntity.sTick == 0) {
                pLevel.playSound(null, pPos, TFCSounds.QUERN_DRAG.get(),
                        SoundSource.BLOCKS, 1, 1 + ((pLevel.random.nextFloat() - pLevel.random.nextFloat()) / 16));
                pBlockEntity.sTick += 85;
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
            pLevel.setBlock(pPos, pState.setValue(auto_quern.ON, false), 3);
        }
    }

    private static void extractEnergy(AutoQuernBlockEntity pBlockEntity) {
        pBlockEntity.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
    }

    private static boolean hasEnoughEnergy(AutoQuernBlockEntity pBlockEntity) {
        return pBlockEntity.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ;
    }

    private static boolean hasRecipe(AutoQuernBlockEntity entity) {
        Level level = entity.level;
        final ItemStack inputStack = entity.inventory.getStackInSlot(1);
        final ItemStackInventory wrapper = new ItemStackInventory(inputStack);
        final QuernRecipe recipe = QuernRecipe.getRecipe(level, wrapper);
        ItemStackHandler inventory = entity.inventory;

        if(recipe != null){
            return recipe.matches(wrapper, level) && canInsertAmountIntoOutputSlot(inventory, recipe.getResult().stack().get().getCount())
                    && canInsertItemIntoOutputSlot(inventory, recipe.getResult().stack().get())
                    && hasToolsInToolSlot(entity);
        }
        return false;
    }


    private static boolean hasToolsInToolSlot(AutoQuernBlockEntity entity) {
        return entity.inventory.getStackInSlot(0).getItem() == ModItems.STEEL_GRINDSTONE.get();
    }

    private static void craftItem(AutoQuernBlockEntity entity) {
        Level level = entity.level;
        final ItemStack inputStack = entity.inventory.getStackInSlot(1);
        final ItemStackInventory wrapper = new ItemStackInventory(inputStack);
        final QuernRecipe recipe = QuernRecipe.getRecipe(level, wrapper);

        if(recipe.matches(wrapper, level)) {
            if(entity.inventory.getStackInSlot(0).hurt(1, RandomSource.create(), null)) {
                entity.inventory.extractItem(0,1, false);
            }
            entity.inventory.extractItem(1,1, false);

            ItemStack outputStack = recipe.getResult().stack().get();
            outputStack.setCount(entity.inventory.getStackInSlot(2).getCount() + recipe.getResult().stack().get().getCount());
            entity.inventory.setStackInSlot(2, outputStack);

            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }
    private void resetSTick() {
        this.sTick = 0;
    }

    private static boolean canInsertItemIntoOutputSlot(ItemStackHandler inventory, ItemStack output) {
        return inventory.getStackInSlot(2).getItem() == output.getItem() || inventory.getStackInSlot(2).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(ItemStackHandler inventory, int output) {
        return inventory.getStackInSlot(2).getMaxStackSize() >= inventory.getStackInSlot(2).getCount() + output; //Stack overflow/loss
    }

    @Override
    public void clearContent() {

    }
}
