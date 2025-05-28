package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.electric_lantern;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.WrappedHandlerEnergy;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.jewey.rosia.Rosia.MOD_ID;

public class ElectricLanternBlockEntity extends TFCBlockEntity {

    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.electric_lantern");

    private int tick;

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricLanternBlockEntity lantern) {
        if((lantern.ENERGY_STORAGE.getEnergyStored() > 0 && lantern.tick == 0 && state.getValue(electric_lantern.ON)))
        {
            level.setBlock(pos, state.setValue(electric_lantern.ON, true), 3);
            lantern.ENERGY_STORAGE.extractEnergy(1, false);
            lantern.tick += 20; // 1 FE/second
        }
        if(lantern.ENERGY_STORAGE.getEnergyStored() <= 0)
        {
            level.setBlock(pos, state.setValue(electric_lantern.ON, false), 3);
        }
        if(lantern.tick > 0)
        {
            lantern.tick -= 1;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            if(side == null) {
                return lazyEnergyHandler.cast();
            }

            if(directionWrappedHandlerMap.containsKey(side)) {
                Direction localDir = this.getBlockState().getValue(electric_lantern.FACING);

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

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(10, 1) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    private final Map<Direction, LazyOptional<WrappedHandlerEnergy>> directionWrappedHandlerMap =
            //Handler for sided energy: extract, receive, canExtract, canReceive
            //Determines what sides can perform actions
            Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> true, (i) -> true, true, true)),
                    Direction.UP, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE,(i) -> true, (i) -> true, true, true)),
                    Direction.NORTH, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
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

    private long lastPlayerTick; // Last player tick this forge was ticked (for purposes of catching up)

    public ElectricLanternBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ELECTRIC_LANTERN_BLOCK_ENTITY.get(), pos, state);
        lastPlayerTick = Integer.MIN_VALUE;
        tick = 0;
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

    @Override
    public void onLoadAdditional() {
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
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
    {;
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        nbt.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

}