package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.mechanical_generator;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.WrappedHandlerEnergy;
import com.jewey.rosia.common.container.MechanicalGeneratorContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotationSinkBlockEntity;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.SinkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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

public class MechanicalGeneratorBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements RotationSinkBlockEntity {

    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.mechanical_generator");

    public static void serverTick(Level level, BlockPos pos, BlockState state, MechanicalGeneratorBlockEntity generator) {
        // Max windmill = 0.79 rad/s = 0.03926991
        // Max waterwheel = 1.45 rad/s = 0.07256133
        int maxCap = generator.ENERGY_STORAGE.getMaxEnergyStored();
        if(generator.isConnectedToNetwork() && generator.node.rotation() != null
                && generator.ENERGY_STORAGE.getEnergyStored() < generator.ENERGY_STORAGE.getMaxEnergyStored()) {
            // 1 FE/tick at max speed of waterwheel
            // 1 FE/4 ticks ~~
            // 0.25 FE/tick at max speed of windmill
            if(generator.tick <= 0) {
                generator.tickRate = (int) (((generator.getRotationSpeed() / 0.07256133) - 1) * (-10));
                generator.tick += generator.tickRate;
                generator.setEnergyLevel(Math.min(generator.ENERGY_STORAGE.getEnergyStored() + 1, maxCap));
            }
            if (generator.tick > 0) {
                generator.tick -= 1;
            }
        }
        else {
            generator.tickRate = 0;
            generator.tick = 0;
        }

        //output energy on block sides
        generator.outputEnergy();
        generator.setChanged();
    }

    public int getTickRate() {
        return tickRate;
    }
    private final ItemStackHandler itemHandler = new ItemStackHandler(0) {};
    private final IntArrayBuilder syncableData;
    private final SinkNode node;
    private int tick;
    public int tickRate;

    public MechanicalGeneratorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MECHANICAL_GENERATOR_BLOCK_ENTITY.get(), pPos, pBlockState, defaultInventory(0), NAME);

        syncableData = new IntArrayBuilder();
        tick = 0;
        tickRate = 0;

        this.node = new SinkNode(pPos, pBlockState.getValue(mechanical_generator.FACING)) {
            @Override
            public String toString()
            {
                return "MechanicalGenerator[pos=%s]".formatted(pos());
            }
        };
    }

    public ContainerData getSyncableData() {
        return syncableData;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer)
    {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        return MechanicalGeneratorContainer.create(this, pPlayerInventory, pContainerId);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @javax.annotation.Nullable Direction side)
    {
        if(cap == ForgeCapabilities.ENERGY) {
            if(side == null) {
                return lazyEnergyHandler.cast();
            }

            if(directionWrappedHandlerMap.containsKey(side)) {
                Direction localDir = this.getBlockState().getValue(mechanical_generator.FACING);

                if(side == Direction.UP || side == Direction.DOWN) {
                    return directionWrappedHandlerMap.get(side).cast();
                }

                return switch (localDir) {
                    default -> directionWrappedHandlerMap.get(side.getOpposite()).cast();
                    case EAST -> directionWrappedHandlerMap.get(side.getClockWise()).cast();
                    case SOUTH -> directionWrappedHandlerMap.get(side).cast();
                    case WEST -> directionWrappedHandlerMap.get(side.getCounterClockWise()).cast();
                };
            }
        }
        return super.getCapability(cap, side);
    }

    // ENERGY STUFF

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(400, maxTransfer) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
        }
        @Override
        public boolean canReceive() {
            return false;
        }
    };

    private final Map<Direction, LazyOptional<WrappedHandlerEnergy>> directionWrappedHandlerMap =
            //Handler for sided energy: extract, receive, canExtract, canReceive (seems redundant, but it breaks otherwise because why not)
            //Determines what sides can perform actions
            //Mechanical Generator NORTH(opposite->south)->Extract Else->N/A
            Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
                    Direction.UP, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE,(i) -> false, (i) -> false, false, false)),
                    Direction.NORTH, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> true, (i) -> false, true, false)),
                    Direction.SOUTH, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
                    Direction.EAST, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
                    Direction.WEST, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)));

    private static final int maxTransfer = 5;

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    public IEnergyStorage getEnergyStorage() {
        return ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
    }

    public void outputEnergy() {
        if (this.ENERGY_STORAGE.getEnergyStored() > 0 && this.ENERGY_STORAGE.canExtract() && toggle) {
            for (final var direction : Direction.values()) {
                final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                if (neighbor == null) {
                    continue;
                }
                neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                    if (neighbor != this && storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                        final int canReceive = Math.min(storage.getMaxEnergyStored() - storage.getEnergyStored(), maxTransfer);
                        final int toSend = MechanicalGeneratorBlockEntity.this.ENERGY_STORAGE.extractEnergy(canReceive,false);
                        final int received = storage.receiveEnergy(toSend, false);

                        MechanicalGeneratorBlockEntity.this.ENERGY_STORAGE.setEnergy(MechanicalGeneratorBlockEntity.this.ENERGY_STORAGE.getEnergyStored() + toSend - received);
                    }
                });
            }
        }
    }

    private boolean toggle = false;

    public InteractionResult togglePush(){
        if(!toggle) {
            toggle = true;
        } else {
            toggle = false;
        }
        return InteractionResult.PASS;
    }

    // ROTATION STUFF

    @Override
    public Node getRotationNode()
    {
        return node;
    }

    public float getRotationSpeed()
    {
        return node.rotation() != null ? Mth.abs(node.rotation().speed()) : 0;
    }

    @Override
    public float getRotationAngle(float partialTick)
    {
        return isConnectedToNetwork() ? RotationSinkBlockEntity.super.getRotationAngle(partialTick) : 0;
    }

    public boolean isConnectedToNetwork()
    {
        return node.rotation() != null;
    }

    @Override
    public void onLoadAdditional() {
        super.onLoadAdditional();
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
        performNetworkAction(NetworkAction.ADD);
    }

    @Override
    protected void onUnloadAdditional()
    {
        performNetworkAction(NetworkAction.REMOVE);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
        toggle = nbt.getBoolean("toggle");
        tickRate = nbt.getInt("tickRate");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        tag.putBoolean("toggle", toggle);
        tag.putInt("tickRate", tickRate);
        super.saveAdditional(tag);
    }

    @Override
    public void clearContent() {

    }
}
