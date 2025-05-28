package com.jewey.rosia.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class DoubleWideBlockItem extends BlockItem {
    public DoubleWideBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        Direction facing = Objects.requireNonNull(pContext.getPlayer()).getDirection();
        BlockPos otherPos = pContext.getClickedPos().relative(facing.getCounterClockWise());
        BlockState otherState = pContext.getLevel().getBlockState(otherPos);
        if(!otherState.isAir()) {
            return InteractionResult.FAIL;
        }
        else return super.place(pContext);
    }
}
