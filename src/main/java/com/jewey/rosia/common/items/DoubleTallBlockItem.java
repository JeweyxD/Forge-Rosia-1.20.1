package com.jewey.rosia.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleTallBlockItem extends BlockItem {
    public DoubleTallBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        BlockPos otherPos = pContext.getClickedPos().relative(Direction.UP);
        BlockState otherState = pContext.getLevel().getBlockState(otherPos);
        if(!otherState.isAir()) {
            return InteractionResult.FAIL;
        }
        else return super.place(pContext);
    }
}
