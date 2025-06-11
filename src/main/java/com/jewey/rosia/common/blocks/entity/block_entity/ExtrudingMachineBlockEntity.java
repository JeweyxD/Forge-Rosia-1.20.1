package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.extruding_machine;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.ExtrudingMachineContainer;
import com.jewey.rosia.common.items.ModItems;
import com.jewey.rosia.config.RosiaConfig;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.recipe.ExtrudingMachineRecipe;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
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


public class ExtrudingMachineBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements MenuProvider
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
                case 0 -> stack.getItem() == ModItems.STEEL_MACHINE_DIE.get();
                case 1 -> stack.getItem() != ModItems.STEEL_MACHINE_DIE.get();  //Prevent multiple tools being inserted
                case 2 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for player interaction
        return switch (slot) {
            case 0 -> stack.getItem() == ModItems.STEEL_MACHINE_DIE.get();
            case 1 -> stack.getItem() != ModItems.STEEL_MACHINE_DIE.get();  //Prevent multiple tools being inserted
            case 2 -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.extruding_machine");
    private int progress = 0;
    private int maxProgress= 120;

    private final IntArrayBuilder syncableData;

    public ExtrudingMachineBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.EXTRUDING_MACHINE_BLOCK_ENTITY.get(), pos, state, defaultInventory(3), NAME);
        syncableData = new IntArrayBuilder() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> ExtrudingMachineBlockEntity.this.progress;
                    case 1 -> ExtrudingMachineBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> ExtrudingMachineBlockEntity.this.progress = value;
                    case 1 -> ExtrudingMachineBlockEntity.this.maxProgress = value;
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
        return ExtrudingMachineContainer.create(this, pPlayerInventory, pContainerId);
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
    private static final int ENERGY_REQ = RosiaConfig.SERVER.ExtrudingMachineEnergyReq.get(); // Energy cost to craft item

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
        tag.putInt("extruding_machine.progress", progress);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag);
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        progress = nbt.getInt("extruding_machine.progress");
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

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, ExtrudingMachineBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity) && hasEnoughEnergy(pBlockEntity) && isHotEnough(pBlockEntity)) {
            pBlockEntity.progress++;
            pLevel.setBlock(pPos, pState.setValue(extruding_machine.ON, true), 3);
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.progress > pBlockEntity.maxProgress) {
                craftItem(pBlockEntity);
                extractEnergy(pBlockEntity);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
            pLevel.setBlock(pPos, pState.setValue(extruding_machine.ON, false), 3);
        }

    }


    private static void extractEnergy(ExtrudingMachineBlockEntity pBlockEntity) {
        pBlockEntity.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
    }

    private static boolean hasEnoughEnergy(ExtrudingMachineBlockEntity pBlockEntity) {
        return pBlockEntity.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ;
    }

    private static boolean isHotEnough(ExtrudingMachineBlockEntity pBlockEntity) {
        final LazyOptional<IHeat> heat = pBlockEntity.inventory.getStackInSlot(1).getCapability(HeatCapability.CAPABILITY);
        return !heat.map(h -> h.getWorkingTemperature() > h.getTemperature()).orElse(false);
    }

    private static boolean hasRecipe(ExtrudingMachineBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.inventory.getSlots());
        for (int i = 0; i < entity.inventory.getSlots(); i++) {
            inventory.setItem(i, entity.inventory.getStackInSlot(i));
        }

        Optional<ExtrudingMachineRecipe> match = level.getRecipeManager()
                .getRecipeFor(ExtrudingMachineRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && canInsertAmountIntoOutputSlot(inventory, match.get().getResultItem(null).getCount())
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem(null))
                && hasToolsInToolSlot(entity);
    }


    private static boolean hasToolsInToolSlot(ExtrudingMachineBlockEntity entity) {
        return entity.inventory.getStackInSlot(0).getItem() == ModItems.STEEL_MACHINE_DIE.get();
    }

    private static void craftItem(ExtrudingMachineBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.inventory.getSlots());
        for (int i = 0; i < entity.inventory.getSlots(); i++) {
            inventory.setItem(i, entity.inventory.getStackInSlot(i));
        }

        Optional<ExtrudingMachineRecipe> match = level.getRecipeManager()
                .getRecipeFor(ExtrudingMachineRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            final ItemStack result = new ItemStack(match.get().getResultItem(null).getItem(),
                    entity.inventory.getStackInSlot(2).getCount() + match.get().getResultItem(null).getCount());
            final LazyOptional<IHeat> heat = entity.inventory.getStackInSlot(1).getCapability(HeatCapability.CAPABILITY);

            if(entity.inventory.getStackInSlot(0).hurt(1, RandomSource.create(), null)) {
                entity.inventory.extractItem(0,1, false);
            }
            entity.inventory.extractItem(1,1, false);
            entity.inventory.setStackInSlot(2, result);

            result.getCapability(HeatCapability.CAPABILITY).ifPresent(outputHeat ->
                    outputHeat.setTemperatureIfWarmer(heat.map(IHeat::getTemperature).orElse(0f)));

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
