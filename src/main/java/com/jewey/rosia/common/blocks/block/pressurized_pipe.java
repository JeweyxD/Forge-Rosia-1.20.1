package com.jewey.rosia.common.blocks.block;

import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.PressurizedPipeBlockEntity;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

public class pressurized_pipe extends DeviceBlock implements DirectionPropertyBlock, IFluidLoggable {
    public pressurized_pipe(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(DirectionPropertyBlock.setAllDirections(getStateDefinition().any(), false));
        registerDefaultState(getStateDefinition().any().setValue(EXTRACT, false));
    }

    public static final BooleanProperty EXTRACT = BooleanProperty.create("extract");

    public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;

    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static
    {
        final VoxelShape north = box(5, 5, 0, 11, 11, 5);
        final VoxelShape south = box(5, 5, 11, 11, 11, 16);
        final VoxelShape west = box(11, 5, 5, 16, 11, 11);
        final VoxelShape east = box(0, 5, 5, 5, 11, 11);
        final VoxelShape up = box(5, 11, 5, 11, 16, 11);
        final VoxelShape down = box(5, 0, 5, 11, 5, 11);

        // Must match Direction.ordinal order
        final VoxelShape[] directions = new VoxelShape[] {down, up, north, south, east, west};

        final VoxelShape center = box(5, 5, 5, 11, 11, 11);

        for (int i = 0; i < SHAPES.length; i++)
        {
            VoxelShape shape = center;
            for (Direction direction : Helpers.DIRECTIONS)
            {
                if (((i >> direction.ordinal()) & 1) == 1)
                {
                    shape = Shapes.or(shape, directions[direction.ordinal()]);
                }
            }
            SHAPES[i] = shape;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos)
    {
        FluidHelpers.tickFluid(level, currentPos, state);
        return updateConnectedSides(level, currentPos, state, null);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        final FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        final BlockState state = updateConnectedSides(context.getLevel(), context.getClickedPos(), defaultBlockState(), context.getNearestLookingDirection());
        if (getFluidProperty().canContain(fluidState.getType()))
        {
            return state.setValue(getFluidProperty(), getFluidProperty().keyFor(fluidState.getType()));
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        if(this == ModBlocks.PRESSURIZED_PIPE.get()) {
            int index = 0;
            for (Direction side : Helpers.DIRECTIONS) {
                if (state.getValue(DirectionPropertyBlock.getProperty(side))) {
                    index |= 1 << side.ordinal();
                }
            }
            return SHAPES[index];
        }
        else return Shapes.block();
    }

    private BlockState updateConnectedSides(LevelAccessor level, BlockPos pos, BlockState state, @Nullable Direction defaultDirection)
    {
        int openSides = 0;
        @Nullable Direction openDirection = null;
        for (final Direction direction : Helpers.DIRECTIONS)
        {
            final BooleanProperty property = DirectionPropertyBlock.getProperty(direction);

            if (defaultDirection == null && state.getValue(property))
            {
                defaultDirection = direction;
            }

            final BlockPos adjacentPos = pos.relative(direction);
            final BlockState adjacentState = level.getBlockState(adjacentPos);
            final boolean adjacentConnection = connectsToPipeInDirection(adjacentState, direction, level, pos);
            if (adjacentConnection)
            {
                openSides++;
                openDirection = direction;
            }

            state = state.setValue(property, adjacentConnection);
        }

        if (openSides == 0)
        {
            // Either we called this method with a non-null default direction, or
            // The state must have already been in-world, which must have had at least one direction previously, which we would have taken as the default
            assert defaultDirection != null;

            return state.setValue(DirectionPropertyBlock.getProperty(defaultDirection), true)
                    .setValue(DirectionPropertyBlock.getProperty(defaultDirection.getOpposite()), true);
        }
        if (openSides == 1)
        {
            // If we only have a single open side, then we always treat this as a straight pipe.
            return state.setValue(DirectionPropertyBlock.getProperty(openDirection.getOpposite()), true);
        }

        return state;
    }

    private boolean connectsToPipeInDirection(BlockState state, Direction direction, LevelAccessor level, BlockPos pos)
    {
        BlockEntity entity = level.getBlockEntity(pos.relative(direction));
        BlockEntity thisEntity = level.getBlockEntity(pos);
        if(entity != null && thisEntity != null
                && entity == ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, pos.relative(direction))
                && thisEntity == ModBlockEntities.PRESSURIZED_PIPE_BLOCK_ENTITY.get().getBlockEntity(level, pos)) {
            if(entity.getBlockState().getValue(EXTRACT) && thisEntity.getBlockState().getValue(EXTRACT)) {
                // Extractors cannot connect to one another
                return false;
            }
        }
        return state.getBlock() == this
                || state.getBlock() == ModBlocks.PRESSURIZED_PIPE.get()
                || state.getBlock() == ModBlocks.ENCASED_PRESSURIZED_PIPE.get()
                || (entity != null && entity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getBlock() != newState.getBlock()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof PressurizedPipeBlockEntity) {
               ( (PressurizedPipeBlockEntity) entity).removeNetworkOnUpdate(state);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(EXTRACT);
        builder.add(PROPERTIES).add(getFluidProperty());
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return IFluidLoggable.super.getFluidLoggedState(state);
    }

    @Override
    public FluidProperty getFluidProperty()
    {
        return FLUID;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if (Helpers.isItem(player.getItemInHand(hand), TFCTags.Items.HAMMERS))
        {
            boolean currentState = state.getValue(EXTRACT);
            level.setBlock(pos, state.setValue(EXTRACT, !currentState), 3);
            Helpers.playPlaceSound(level, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PressurizedPipeBlockEntity(pPos, pState);
    }
}
