package com.jewey.rosia.common.blocks.entity;

import com.jewey.rosia.common.capabilities.ResettableCapability;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/*
 * Part of the code from Immersive Engineering:
 * https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18.2/src/main/java/blusunrize/immersiveengineering/common/blocks/IEBaseBlockEntity.java
 */

public abstract class MultiblockBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> {

    public boolean isDummy;

    public MultiblockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, InventoryFactory<ItemStackHandler> inventory, Component defaultName) {
        super(type, pos, state, inventory, defaultName);
    }

    private final List<ResettableCapability<?>> caps = new ArrayList<>();
    private final List<Runnable> onCapInvalidate = new ArrayList<>();

    protected <T> ResettableCapability<T> registerCapability(T val) {
        ResettableCapability<T> cap = new ResettableCapability<>(val);
        caps.add(cap);
        return cap;
    }

    protected ResettableCapability<IEnergyStorage> registerEnergyStorage(IEnergyStorage storage) {
        return registerCapability(storage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        resetAllCaps();
        caps.clear();
        onCapInvalidate.forEach(Runnable::run);
        onCapInvalidate.clear();
    }

    protected void resetAllCaps() {
        caps.forEach(ResettableCapability::reset);
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        isDummy = nbt.getBoolean("dummy");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putBoolean("dummy", isDummy);
        super.saveAdditional(nbt);
    }

    @Nullable
    public abstract MultiblockBlockEntity master();
}
