package com.jewey.rosia.common.blocks.block;

import com.jewey.rosia.common.blocks.MultiblockDevice;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.FridgeBlockEntity;
import com.jewey.rosia.common.items.DoubleTallBlockItem;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.util.MultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiPredicate;
import java.util.function.Supplier;


public class fridge extends MultiblockDevice
{
    public fridge(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.DROP);
    }

    public Supplier<BlockItem> blockItemSupplier() {
        return () -> new DoubleTallBlockItem(this, new Item.Properties());
    }

    @Override
    public Direction getDummyOffsetDir(BlockState state) {
        return Direction.UP;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        FridgeBlockEntity fridge = ModBlockEntities.FRIDGE_BLOCK_ENTITY.get().create(pos, state);
        assert fridge != null;
        fridge.isDummy = state.getValue(DUMMY);
        return fridge;
    }

    // Voxel Shape
    private static final VoxelShape SHAPE = Block.box(0,0,0,16,16,16);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}