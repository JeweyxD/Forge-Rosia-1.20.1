package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.power_conduit;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import net.dries007.tfc.common.blockentities.TickableBlockEntity;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.jewey.rosia.Rosia.MOD_ID;

public class PowerConduitBlockEntity extends TickableBlockEntity {
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.power_conduit");

    public float energyToNetwork; // How much energy is read by the extractor to the network
    public float energyFromNetwork; // How much energy is read by non-extractors from the network
    public float energyRequest; // How much energy is needed to fill all neighbor storages
    public float networkReset; // Counting value for when the network needs to be removed due to a break in the network
    public int[] networkID; // The ID of the network. This doesn't work if the block is placed at (X 0, Y 999, Z 0)... so don't do that
    public static int[] NO_NETWORK = new int[]{0, 999, 0}; // Default ID for a nonexistent network
    public float SPEED = 50; // Amount of FE per tick can be received/extracted (x20 for per second)

    public PowerConduitBlockEntity(BlockPos pos, BlockState state){
        super(ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get(), pos, state);
        energyToNetwork = 0;
        energyFromNetwork = 0;
        energyRequest = 0;
        networkReset = 0;
        networkID = NO_NETWORK;
    }


    public static void serverTick(Level level, BlockPos pos, BlockState state, PowerConduitBlockEntity conduit) {
        if(Arrays.equals(conduit.networkID, NO_NETWORK)) {
            if(state.getValue(power_conduit.EXTRACT)) {
                if(conduit.networkReset == 0) {
                    conduit.energyToNetwork(state);
                    if (conduit.energyToNetwork != 0) {
                        conduit.createNetwork(state);
                    }
                }
            }
            else conduit.getNetwork(state);
        }

        if(!Arrays.equals(conduit.networkID, NO_NETWORK) && !state.getValue(power_conduit.EXTRACT)) {
            int X = conduit.networkID[0];
            int Y = conduit.networkID[1];
            int Z = conduit.networkID[2];
            BlockPos networkPos = new BlockPos(X, Y, Z);
            var networkEntity = ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);
            if(networkEntity != null && networkEntity.networkReset != 0) {
                conduit.networkID = NO_NETWORK;
            }
        }

        if(state.getValue(power_conduit.EXTRACT)) {
            conduit.energyFromNetwork = 0;
            conduit.energyRequest = 0;
            if(conduit.networkReset == 0) {
                conduit.energyToNetwork(state);
            }
            if(conduit.networkReset > 0) {
                conduit.networkReset -= 1;
            }
        }
        if(!state.getValue(power_conduit.EXTRACT)) {
            conduit.energyToNetwork = 0;
            conduit.energyFromNetwork(level, state);
            if(conduit.energyFromNetwork == 0) {
                conduit.removeNetwork(state);
            }
            conduit.energyRequest(state);
        }
    }

    /*
    createNetwork() -> Create a new network ID from an extractor's XYZ coordinates
    removeNetwork() -> Delete a network when empty so a new one can be made and continue to supply energy throughout all connected conduits
    removeNetworkOnUpdate() -> Delete a network when a non-extractor is broken, avoiding illogical connections
    getNetwork() -> Copy a neighbor's network ID, adding this conduit to an existing network
    energyToNetwork() -> Find how much the extractor can access from all neighbors
    energyFromNetwork() -> Get the amount of energy in the network
    energyRequest() -> Find how much all neighbors need to fill storages, fill storages, drain sources

    The network will fulfill the request from the closest non-extractor first, or the first in the update chain,
    by a flashed value of the transfer speed (or the total available energy to the network, whichever is smaller).
    Any remainder energy from the flashed value not used by the first in the chain will be consumed by the second, third, etc.
    This aims to prevent energy duping.
    If a non-extractor is broken, the whole chain must be reset to the default ID and re-check for the network via its neighbors.
    We do this to avoid any illogical connections, where a conduit would not physically be able to get energy from the network's sources.
    AKA no quantum-entanglement/teleportation nonsense lol.
    */

    // FUCK THIS WAS HARD... AGAIN

    public void createNetwork(BlockState state) {
        if(state.getValue(power_conduit.EXTRACT)) {
            int X = this.getBlockPos().getX();
            int Y = this.getBlockPos().getY();
            int Z = this.getBlockPos().getZ();
            networkID = new int[]{X, Y, Z}; // The extractors networkID
        }
    }

    public void removeNetwork(BlockState state) {
        // This serves to delete a network when empty so a new one can be made and continue to supply energy throughout all connected conduits
        if(state.getValue(power_conduit.EXTRACT) && this.energyToNetwork == 0) {
            networkID = NO_NETWORK; // Reset extractor to default "no network" when there's no energy to supply
        }
        if(!Arrays.equals(this.networkID, NO_NETWORK) && !state.getValue(power_conduit.EXTRACT)) {
            int X = this.networkID[0];
            int Y = this.networkID[1];
            int Z = this.networkID[2];

            BlockPos networkPos = new BlockPos(X, Y, Z);
            assert level != null;
            var networkEntity = ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

           if(networkEntity != null && networkEntity.energyToNetwork == 0 && Arrays.equals(networkEntity.networkID, NO_NETWORK)) {
               networkID = NO_NETWORK; // Reset non-extractor to default "no network" when extractor has no energy to supply
           }
           if(networkEntity == null) {
               networkID = NO_NETWORK; // Reset non-extractor to default "no network" when extractor doesn't exist
           }
        }
    }

    public void removeNetworkOnUpdate(BlockState state) {
        // This serves to delete a network in the event that a conduit in the chain is broken, which could lead to a possible hole in the chain
        // where remaining non-extractors are still receiving from an extractor they could not physically reach
        if(!Arrays.equals(this.networkID, NO_NETWORK) && !state.getValue(power_conduit.EXTRACT)) {
            int X = this.networkID[0];
            int Y = this.networkID[1];
            int Z = this.networkID[2];

            BlockPos networkPos = new BlockPos(X, Y, Z);
            assert level != null;
            var networkEntity = ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

            if(networkEntity != null) {
                networkEntity.networkReset = 20; // Set the counting value; the extractor will reset after this many ticks
                networkEntity.energyToNetwork = 0;
                networkEntity.networkID = NO_NETWORK; // Reset the extractor to default "no network"
                networkID = NO_NETWORK; // Reset non-extractor to default "no network"
            }
        }
    }

    public void getNetwork(BlockState state) {
        for (Direction direction : Helpers.DIRECTIONS) {
            if (state.getValue(DirectionPropertyBlock.getProperty(direction))) {
                assert this.level != null;
                final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                if (neighbor == null) {
                    continue;
                }
                PowerConduitBlockEntity neighborEntity = ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get().getBlockEntity(level, neighbor.getBlockPos());
                if (neighborEntity == null) {
                    continue;
                }
                if(Arrays.equals(this.networkID, NO_NETWORK) && !Arrays.equals(neighborEntity.networkID, NO_NETWORK)) {
                    if (neighbor == ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get().getBlockEntity(level, neighbor.getBlockPos())) {
                        this.networkID = neighborEntity.networkID; // Copy the neighbors networkID
                    }
                }
            }
        }
    }

    public void energyToNetwork(BlockState state) {
        final int[] D = {0};
        final int[] U = {0};
        final int[] N = {0};
        final int[] S = {0};
        final int[] W = {0};
        final int[] E = {0};
        if(state.getValue(power_conduit.EXTRACT)) {
            for (Direction direction : Helpers.DIRECTIONS) {
                if(state.getValue(DirectionPropertyBlock.getProperty(direction))){
                    assert this.level != null;
                    final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                    if (neighbor == null) {
                        continue;
                    }
                    neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                        if (neighbor != this && storage.getEnergyStored() != 0) {
                            // Check if the neighbor storage has a limited sided handler and limited transfer speed
                            int maxTransfer = Math.min(storage.extractEnergy((int) SPEED, true), storage.getEnergyStored());
                            if (storage.canExtract()) {
                                if(direction == Direction.DOWN) {D[0] = maxTransfer;}
                                if(direction == Direction.UP) {U[0] = maxTransfer;}
                                if(direction == Direction.NORTH) {N[0] = maxTransfer;}
                                if(direction == Direction.SOUTH) {S[0] = maxTransfer;}
                                if(direction == Direction.WEST) {W[0] = maxTransfer;}
                                if(direction == Direction.EAST) {E[0] = maxTransfer;}
                            }
                        }
                    });
                }
            }
            // Find total energy from all neighbors with which to extract from, but limit to transfer speed to avoid duping energy later
            float storageNeighborCount = 0; // Total number of neighbor storages that we are trying to drain
            if (D[0] > 0) {storageNeighborCount++;}
            if (U[0] > 0) {storageNeighborCount++;}
            if (N[0] > 0) {storageNeighborCount++;}
            if (S[0] > 0) {storageNeighborCount++;}
            if (W[0] > 0) {storageNeighborCount++;}
            if (E[0] > 0) {storageNeighborCount++;}
            float[] array = {D[0], U[0], N[0], S[0], W[0], E[0]};
            Arrays.sort(array);
            // Smallest amount we can pull at once from each neighbor storage
            float maxDrain = array[Mth.clamp(6 - (int) storageNeighborCount, 0, 5)];
            energyToNetwork = Math.min((maxDrain * storageNeighborCount), SPEED);

            // Remove this extractors network if it has no energy to add to the network
            if (energyToNetwork == 0 && !Arrays.equals(networkID, NO_NETWORK)) {
                removeNetwork(state);
            }
        }
        else energyToNetwork = 0; removeNetwork(state);
    }

    public void energyFromNetwork(Level level, BlockState state) {
        if(!Arrays.equals(this.networkID, NO_NETWORK) && !state.getValue(power_conduit.EXTRACT)) {
            int X = this.networkID[0];
            int Y = this.networkID[1];
            int Z = this.networkID[2];

            BlockPos networkPos = new BlockPos(X, Y, Z);
            var networkEntity = ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

            if(networkEntity != null) {
                energyFromNetwork = networkEntity.energyToNetwork;
            }
            else energyFromNetwork = 0;
        }
        else energyFromNetwork = 0;
    }

    public void energyRequest(BlockState state) {
        final float[] D = {0};
        final float[] U = {0};
        final float[] N = {0};
        final float[] S = {0};
        final float[] W = {0};
        final float[] E = {0};
        final float[] D2 = {0};
        final float[] U2 = {0};
        final float[] N2 = {0};
        final float[] S2 = {0};
        final float[] W2 = {0};
        final float[] E2 = {0};
        if(!state.getValue(power_conduit.EXTRACT)) {
            for (Direction direction : Helpers.DIRECTIONS) {
                if (state.getValue(DirectionPropertyBlock.getProperty(direction))) {
                    assert this.level != null;
                    final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                    if (neighbor == null) {
                        continue;
                    }
                    neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                        if (neighbor != this && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                            // Check if the neighbor storage has a limited sided handler
                            // Check if the neighbor has a lower transfer speed than the network
                            float storageCanReceive = Math.min(storage.getMaxEnergyStored() - storage.getEnergyStored(), storage.receiveEnergy((int) SPEED, true));
                            if (storage.canReceive()) {
                                if(direction == Direction.DOWN) {D[0] = storageCanReceive;}
                                if(direction == Direction.UP) {U[0] = storageCanReceive;}
                                if(direction == Direction.NORTH) {N[0] = storageCanReceive;}
                                if(direction == Direction.SOUTH) {S[0] = storageCanReceive;}
                                if(direction == Direction.WEST) {W[0] = storageCanReceive;}
                                if(direction == Direction.EAST) {E[0] = storageCanReceive;}
                            }
                        }
                    });
                }
            }
            energyRequest = D[0] + U[0] + N[0] + S[0] + W[0] + E[0]; // Total amount of energy needed to fill all neighbor storages

            float storageNeighborCount = 0; // Total number of neighbor storages that we are trying to fill
            if (D[0] > 0) {storageNeighborCount++;}
            if (U[0] > 0) {storageNeighborCount++;}
            if (N[0] > 0) {storageNeighborCount++;}
            if (S[0] > 0) {storageNeighborCount++;}
            if (W[0] > 0) {storageNeighborCount++;}
            if (E[0] > 0) {storageNeighborCount++;}

            int X = this.networkID[0];
            int Y = this.networkID[1];
            int Z = this.networkID[2];
            BlockPos networkPos = new BlockPos(X, Y, Z);
            assert this.level != null;
            var networkEntity = ModBlockEntities.POWER_CONDUIT_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

            float[] array = {D[0], U[0], N[0], S[0], W[0], E[0]};
            Arrays.sort(array);
            // Smallest storage space to fill, prevent overflow if the average among the storages is higher than this
            float maxFillNoOverflow = array[Mth.clamp(6 - (int) storageNeighborCount, 0, 5)];
            // Smallest amount we can fill, taken from the network
            float maxFillAvailable = Math.min(maxFillNoOverflow, (energyFromNetwork / storageNeighborCount));

            // How much each neighbor will get filled from the total available, also accounting for transfer speed
            int fillEachNeighbor = (int) Math.min(Math.min((energyRequest / storageNeighborCount), maxFillAvailable), SPEED);
            int totalToConsume = (int) (fillEachNeighbor * storageNeighborCount);

            // Fulfill the energyRequest:
            // Fill each neighbor storage
            for (Direction direction : Helpers.DIRECTIONS) {
                assert this.level != null;
                if (state.getValue(DirectionPropertyBlock.getProperty(direction))) {
                    final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                    if (neighbor == null) {
                        continue;
                    }
                    neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                        if (neighbor != this && storage.getEnergyStored() < storage.getMaxEnergyStored()
                                && networkEntity != null && energyFromNetwork != 0) {
                            // Check if the neighbor storage has a limited sided handler
                            if(storage.canReceive()) {
                                storage.receiveEnergy(fillEachNeighbor, false);
                                // Subtract from the extractors energyToNetwork so other non-extractors can't pull faster than the transfer speed,
                                // and to stop energy duping
                                networkEntity.energyToNetwork -= totalToConsume;
                            }
                        }
                    });
                }
            }
            // Drain extractor's neighbor storages
            if(!Arrays.equals(this.networkID, NO_NETWORK)) {
                for (Direction direction : Helpers.DIRECTIONS) {
                    if (networkEntity.getBlockState().getValue(DirectionPropertyBlock.getProperty(direction))) {
                        assert networkEntity.level != null;
                        final BlockEntity networkEntityNeighbor = networkEntity.level.getBlockEntity(networkEntity.worldPosition.relative(direction));
                        if (networkEntityNeighbor == null) {
                            continue;
                        }
                        networkEntityNeighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                            float storageCanExtract = storage.getEnergyStored();
                            // Check if the neighbor storage has a limited sided handler
                            if (storage.canExtract()) {
                                if(direction == Direction.DOWN) {D2[0] = storageCanExtract;}
                                if(direction == Direction.UP) {U2[0] = storageCanExtract;}
                                if(direction == Direction.NORTH) {N2[0] = storageCanExtract;}
                                if(direction == Direction.SOUTH) {S2[0] = storageCanExtract;}
                                if(direction == Direction.WEST) {W2[0] = storageCanExtract;}
                                if(direction == Direction.EAST) {E2[0] = storageCanExtract;}
                            }
                        });
                    }
                }
                float neighborSourceCount = 0;
                if (D2[0] > 0) {neighborSourceCount++;}
                if (U2[0] > 0) {neighborSourceCount++;}
                if (N2[0] > 0) {neighborSourceCount++;}
                if (S2[0] > 0) {neighborSourceCount++;}
                if (W2[0] > 0) {neighborSourceCount++;}
                if (E2[0] > 0) {neighborSourceCount++;}

                // How much each neighbor will get drained
                int drainEachNeighbor = (int) (totalToConsume / neighborSourceCount);
                // Remainder to account for and prevent rounding errors
                final int[] remainder = {(int) (totalToConsume - (drainEachNeighbor * neighborSourceCount))};

                for (Direction direction : Helpers.DIRECTIONS) {
                    if (networkEntity.getBlockState().getValue(DirectionPropertyBlock.getProperty(direction))) {
                        assert networkEntity.level != null;
                        final BlockEntity networkEntityNeighbor = networkEntity.level.getBlockEntity(networkEntity.worldPosition.relative(direction));
                        if (networkEntityNeighbor == null) {
                            continue;
                        }
                        networkEntityNeighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                            if (networkEntityNeighbor != networkEntity) {
                                // Check if the neighbor storage has a limited sided handler
                                if (storage.canExtract()) {
                                    storage.extractEnergy(drainEachNeighbor, false);
                                }
                                // Drain remainder from the first applicable storage
                                if (remainder[0] != 0 && storage.canExtract() && storage.extractEnergy(remainder[0], true) == remainder[0]) {
                                    storage.extractEnergy(remainder[0], false);
                                    remainder[0] = 0;
                                }
                            }
                        });
                    }
                }
            }
            assert level != null;
            energyFromNetwork(level, state);
        }
        else if(state.getValue(power_conduit.EXTRACT)) {
            energyRequest = 0;
        }
    }

    @Override
    public void onLoadAdditional() {
        super.onLoadAdditional();
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        energyToNetwork = nbt.getFloat("energyToNetwork");
        energyFromNetwork = nbt.getFloat("energyFromNetwork");
        energyRequest = nbt.getFloat("energyRequest");
        networkID = nbt.getIntArray("networkID");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putFloat("energyToNetwork", energyToNetwork);
        nbt.putFloat("energyFromNetwork", energyFromNetwork);
        nbt.putFloat("energyRequest", energyRequest);
        nbt.putIntArray("networkID", networkID);
        super.saveAdditional(nbt);
    }
}
