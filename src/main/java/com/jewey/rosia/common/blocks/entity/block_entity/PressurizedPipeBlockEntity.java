package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.block.pressurized_pipe;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.jewey.rosia.Rosia.MOD_ID;

public class PressurizedPipeBlockEntity extends TickableBlockEntity {
    public @NotNull Component getDisplayName() {
        return NAME;
    }
    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.pressurized_pipe");

    public float fluidToNetwork; // How much fluid is read by the extractor to the network
    public float fluidFromNetwork; // How much fluid is read by non-extractors from the network
    public float fluidRequest; // How much fluid is needed to fill all neighbor tanks
    public float networkReset; // Counting value for when the network needs to be removed due to a break in the network
    public int[] networkID; // The ID of the network. This doesn't work if the block is placed at (X 0, Y 999, Z 0)... so don't do that
    public static int[] NO_NETWORK = new int[]{0, 999, 0}; // Default ID for a nonexistent network
    public float SPEED = 50; // Amount of mB per tick can be filled/drained (x20 for per second)

    public PressurizedPipeBlockEntity(BlockPos pos, BlockState state){
        super(ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get(), pos, state);
        fluidToNetwork = 0;
        fluidFromNetwork = 0;
        fluidRequest = 0;
        networkReset = 0;
        networkID = NO_NETWORK;
    }


    public static void serverTick(Level level, BlockPos pos, BlockState state, PressurizedPipeBlockEntity pipe) {
        if(Arrays.equals(pipe.networkID, NO_NETWORK)) {
            if(state.getValue(pressurized_pipe.EXTRACT)) {
                if(pipe.networkReset == 0) {
                    pipe.fluidToNetwork(state);
                    if (pipe.fluidToNetwork != 0) {
                        pipe.createNetwork(state);
                    }
                }
            }
            else pipe.getNetwork(state);
        }

        if(!Arrays.equals(pipe.networkID, NO_NETWORK) && !state.getValue(pressurized_pipe.EXTRACT)) {
            int X = pipe.networkID[0];
            int Y = pipe.networkID[1];
            int Z = pipe.networkID[2];
            BlockPos networkPos = new BlockPos(X, Y, Z);
            var networkEntity = ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);
            if(networkEntity != null && networkEntity.networkReset != 0) {
                pipe.networkID = NO_NETWORK;
            }
        }

        if(state.getValue(pressurized_pipe.EXTRACT)) {
            pipe.fluidFromNetwork = 0;
            pipe.fluidRequest = 0;
            if(pipe.networkReset == 0) {
                pipe.fluidToNetwork(state);
            }
            if(pipe.networkReset > 0) {
                pipe.networkReset -= 1;
            }
        }
        if(!state.getValue(pressurized_pipe.EXTRACT)) {
            pipe.fluidToNetwork = 0;
            pipe.fluidFromNetwork(level, state);
            if(pipe.fluidFromNetwork == 0) {
                pipe.removeNetwork(state);
            }
            pipe.fluidRequest(state);
        }
    }

    /*
    createNetwork() -> Create a new network ID from an extractor's XYZ coordinates
    removeNetwork() -> Delete a network when empty so a new one can be made and continue to supply fluid throughout all connected pipes
    removeNetworkOnUpdate() -> Delete a network when a non-extractor is broken, avoiding illogical connections
    getNetwork() -> Copy a neighbor's network ID, adding this pipe to an existing network
    fluidToNetwork() -> Find how much the extractor can access from all neighbors
    fluidFromNetwork() -> Get the amount of fluid in the network
    fluidRequest() -> Find how much all neighbors need to fill tanks, fill tanks, drain sources

    The network will fulfill the request from the closest non-extractor first, or the first in the update chain,
    by a flashed value of the transfer speed (or the total available fluid to the network, whichever is smaller).
    Any remainder fluid from the flashed value not used by the first in the chain will be consumed by the second, third, etc.
    This aims to prevent fluid duping, as well as a more "realistic" fluid flow mechanic.
    If a non-extractor is broken, the whole chain must be reset to the default ID and re-check for the network via its neighbors.
    We do this to avoid any illogical connections, where a pipe would not physically be able to get fluid from the network's sources.
    AKA no quantum-entanglement/teleportation nonsense lol.
    */

    // FUCK THIS WAS HARD

    public void createNetwork(BlockState state) {
        if(state.getValue(pressurized_pipe.EXTRACT)) {
            int X = this.getBlockPos().getX();
            int Y = this.getBlockPos().getY();
            int Z = this.getBlockPos().getZ();
            networkID = new int[]{X, Y, Z}; // The extractors networkID
        }
    }

    public void removeNetwork(BlockState state) {
        // This serves to delete a network when empty so a new one can be made and continue to supply fluid throughout all connected pipes
        if(state.getValue(pressurized_pipe.EXTRACT) && this.fluidToNetwork == 0) {
            networkID = NO_NETWORK; // Reset extractor to default "no network" when there's no fluid to supply
        }
        if(!Arrays.equals(this.networkID, NO_NETWORK) && !state.getValue(pressurized_pipe.EXTRACT)) {
            int X = this.networkID[0];
            int Y = this.networkID[1];
            int Z = this.networkID[2];

            BlockPos networkPos = new BlockPos(X, Y, Z);
            assert level != null;
            var networkEntity = ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

           if(networkEntity != null && networkEntity.fluidToNetwork == 0 && Arrays.equals(networkEntity.networkID, NO_NETWORK)) {
               networkID = NO_NETWORK; // Reset non-extractor to default "no network" when extractor has no fluid to supply
           }
           if(networkEntity == null) {
               networkID = NO_NETWORK; // Reset non-extractor to default "no network" when extractor doesn't exist
           }
        }
    }

    public void removeNetworkOnUpdate(BlockState state) {
        // This serves to delete a network in the event that a pipe in the chain is broken, which could lead to a possible hole in the chain
        // where remaining non-extractors are still receiving from an extractor they could not physically reach
        if(!Arrays.equals(this.networkID, NO_NETWORK) && !state.getValue(pressurized_pipe.EXTRACT)) {
            int X = this.networkID[0];
            int Y = this.networkID[1];
            int Z = this.networkID[2];

            BlockPos networkPos = new BlockPos(X, Y, Z);
            assert level != null;
            var networkEntity = ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

            if(networkEntity != null) {
                networkEntity.networkReset = 20; // Set the counting value; the extractor will reset after this many ticks
                networkEntity.fluidToNetwork = 0;
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
                PressurizedPipeBlockEntity neighborEntity = ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, neighbor.getBlockPos());
                if (neighborEntity == null) {
                    continue;
                }
                if(Arrays.equals(this.networkID, NO_NETWORK) && !Arrays.equals(neighborEntity.networkID, NO_NETWORK)) {
                    if (neighbor == ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, neighbor.getBlockPos())) {
                        this.networkID = neighborEntity.networkID; // Copy the neighbors networkID
                    }
                }
            }
        }
    }

    public void fluidToNetwork(BlockState state) {
        final int[] D = {0};
        final int[] U = {0};
        final int[] N = {0};
        final int[] S = {0};
        final int[] W = {0};
        final int[] E = {0};
        if(state.getValue(pressurized_pipe.EXTRACT)) {
            for (Direction direction : Helpers.DIRECTIONS) {
                if(state.getValue(DirectionPropertyBlock.getProperty(direction))){
                    assert this.level != null;
                    final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                    if (neighbor == null) {
                        continue;
                    }
                    neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(storage -> {
                        if (neighbor != this && !storage.getFluidInTank(1).isEmpty() && storage.getFluidInTank(1).getFluid() == Fluids.WATER) {
                            // Check if the neighbor tank has a limited sided handler and limited transfer speed
                            int maxTransfer = Math.min(storage.drain((int) SPEED, IFluidHandler.FluidAction.SIMULATE).getAmount(),
                                    storage.getFluidInTank(1).getAmount());
                            FluidStack simStack = new FluidStack(Fluids.WATER, 1);
                            if (storage.drain(1, IFluidHandler.FluidAction.SIMULATE).equals(simStack)) {
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
            // Find total fluid from all neighbors with which to extract from, but limit to transfer speed to avoid duping fluid later
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
            fluidToNetwork = Math.min((maxDrain * storageNeighborCount), SPEED);

            // Remove this extractors network if it has no fluid to add to the network
            if (fluidToNetwork == 0 && !Arrays.equals(networkID, NO_NETWORK)) {
                removeNetwork(state);
            }
        }
        else fluidToNetwork = 0; removeNetwork(state);
    }

    public void fluidFromNetwork(Level level, BlockState state) {
        if(!Arrays.equals(this.networkID, NO_NETWORK) && !state.getValue(pressurized_pipe.EXTRACT)) {
            int X = this.networkID[0];
            int Y = this.networkID[1];
            int Z = this.networkID[2];

            BlockPos networkPos = new BlockPos(X, Y, Z);
            var networkEntity = ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

            if(networkEntity != null) {
                fluidFromNetwork = networkEntity.fluidToNetwork;
            }
            else fluidFromNetwork = 0;
        }
        else fluidFromNetwork = 0;
    }

    public void fluidRequest(BlockState state) {
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
        if(!state.getValue(pressurized_pipe.EXTRACT)) {
            for (Direction direction : Helpers.DIRECTIONS) {
                if (state.getValue(DirectionPropertyBlock.getProperty(direction))) {
                    assert this.level != null;
                    final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                    if (neighbor == null) {
                        continue;
                    }
                    neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(storage -> {
                        if (neighbor != this && storage.getFluidInTank(1).getAmount() < storage.getTankCapacity(1)) {
                            // Check if the neighbor tank has a limited sided handler
                            // Check if the neighbor has a lower transfer speed than the network
                            FluidStack simStack = new FluidStack(Fluids.WATER, 1);
                            float storageCanReceive = Math.min(storage.getTankCapacity(1) - storage.getFluidInTank(1).getAmount(),
                                    storage.fill(new FluidStack(Fluids.WATER, (int) SPEED), IFluidHandler.FluidAction.SIMULATE));
                            if (storage.fill(simStack, IFluidHandler.FluidAction.SIMULATE) == 1) {
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
            fluidRequest = D[0] + U[0] + N[0] + S[0] + W[0] + E[0]; // Total amount of fluid needed to fill all neighbor tanks

            float storageNeighborCount = 0; // Total number of neighbor tanks that we are trying to fill
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
            var networkEntity = ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, networkPos);

            float[] array = {D[0], U[0], N[0], S[0], W[0], E[0]};
            Arrays.sort(array);
            // Smallest tank space to fill, prevent overflow if the average among the tanks is higher than this
            float maxFillNoOverflow = array[Mth.clamp(6 - (int) storageNeighborCount, 0, 5)];
            // Smallest amount we can fill, taken from the network
            float maxFillAvailable = Math.min(maxFillNoOverflow, (fluidFromNetwork / storageNeighborCount));

            // How much each neighbor will get filled from the total available, also accounting for transfer speed
            int fillEachNeighbor = (int) Math.min(Math.min((fluidRequest / storageNeighborCount), maxFillAvailable), SPEED);
            int totalToConsume = (int) (fillEachNeighbor * storageNeighborCount);

            // Fulfill the fluidRequest:
            // Fill each neighbor tank
            for (Direction direction : Helpers.DIRECTIONS) {
                assert this.level != null;
                if (state.getValue(DirectionPropertyBlock.getProperty(direction))) {
                    final BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
                    if (neighbor == null) {
                        continue;
                    }
                    neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(storage -> {
                        if (neighbor != this && storage.getFluidInTank(1).getAmount() < storage.getTankCapacity(1)
                                && networkEntity != null && fluidFromNetwork != 0) {
                            // Check if the neighbor tank has a limited sided handler
                            FluidStack simStack = new FluidStack(Fluids.WATER, 1);
                            if(storage.fill(simStack, IFluidHandler.FluidAction.SIMULATE) == 1) {
                                FluidStack fillStack = new FluidStack(Fluids.WATER, fillEachNeighbor);
                                storage.fill(fillStack, IFluidHandler.FluidAction.EXECUTE);
                                // Subtract from the extractors fluidToNetwork so other non-extractors can't pull faster than the transfer speed,
                                // and to stop fluid duping
                                networkEntity.fluidToNetwork -= totalToConsume;
                            }
                        }
                    });
                }
            }
            // Drain extractor's neighbor tanks
            if(!Arrays.equals(this.networkID, NO_NETWORK)) {
                for (Direction direction : Helpers.DIRECTIONS) {
                    if (networkEntity.getBlockState().getValue(DirectionPropertyBlock.getProperty(direction))) {
                        assert networkEntity.level != null;
                        final BlockEntity networkEntityNeighbor = networkEntity.level.getBlockEntity(networkEntity.worldPosition.relative(direction));
                        if (networkEntityNeighbor == null) {
                            continue;
                        }
                        networkEntityNeighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(storage -> {
                            float storageCanExtract = storage.getFluidInTank(1).getAmount();
                            // Check if the neighbor tank has a limited sided handler
                            FluidStack simStack = new FluidStack(Fluids.WATER, 1);
                            if (storage.drain(1, IFluidHandler.FluidAction.SIMULATE).equals(simStack)) {
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
                        networkEntityNeighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(storage -> {
                            if (networkEntityNeighbor != networkEntity) {
                                // Check if the neighbor tank has a limited sided handler
                                FluidStack simStack = new FluidStack(Fluids.WATER, 1);
                                if (storage.drain(1, IFluidHandler.FluidAction.SIMULATE).equals(simStack)) {
                                    FluidStack drainStack = new FluidStack(Fluids.WATER, drainEachNeighbor);
                                    storage.drain(drainStack, IFluidHandler.FluidAction.EXECUTE);
                                }
                                // Drain remainder from the first applicable tank
                                FluidStack remainderStack = new FluidStack(Fluids.WATER, remainder[0]);
                                if (remainder[0] != 0 && storage.drain(1, IFluidHandler.FluidAction.SIMULATE).equals(simStack)
                                        && storage.drain(remainderStack, IFluidHandler.FluidAction.SIMULATE).getAmount() == remainder[0]) {
                                    storage.drain(remainderStack, IFluidHandler.FluidAction.EXECUTE);
                                    remainder[0] = 0;
                                }
                            }
                        });
                    }
                }
            }
            assert level != null;
            fluidFromNetwork(level, state);
        }
        else if(state.getValue(pressurized_pipe.EXTRACT)) {
            fluidRequest = 0;
        }
    }

    @Override
    public void onLoadAdditional() {
        super.onLoadAdditional();
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        fluidToNetwork = nbt.getFloat("fluidToNetwork");
        fluidFromNetwork = nbt.getFloat("fluidFromNetwork");
        fluidRequest = nbt.getFloat("fluidRequest");
        networkID = nbt.getIntArray("networkID");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putFloat("fluidToNetwork", fluidToNetwork);
        nbt.putFloat("fluidFromNetwork", fluidFromNetwork);
        nbt.putFloat("fluidRequest", fluidRequest);
        nbt.putIntArray("networkID", networkID);
        super.saveAdditional(nbt);
    }
}
