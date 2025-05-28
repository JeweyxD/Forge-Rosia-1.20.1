package com.jewey.rosia.common.blocks.entity;

import com.jewey.rosia.util.ModEnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.Predicate;

public class WrappedHandlerEnergy implements IEnergyStorage {

    private final ModEnergyStorage handler;
    private final Predicate<Integer> extract;
    private final Predicate<Integer> receive;
    private final boolean canExtract;
    private final boolean canReceive;

    public WrappedHandlerEnergy(ModEnergyStorage handler, Predicate<Integer> extract, Predicate<Integer> receive, boolean canExtract, boolean canReceive) {
        this.handler = handler;
        this.extract = extract;
        this.receive = receive;
        this.canExtract = canExtract;
        this.canReceive = canReceive;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return this.receive.test(maxReceive) ? this.handler.receiveEnergy(maxReceive, simulate) : maxReceive;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return this.extract.test(maxExtract) ? this.handler.extractEnergy(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return this.handler.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return this.handler.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return this.canExtract;
    }

    @Override
    public boolean canReceive() {
        return this.canReceive;
    }
}
