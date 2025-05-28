package com.jewey.rosia.common.items;

import com.jewey.rosia.common.entities.LocomotiveEntity;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class LocomotiveItem extends Item {
    private final Supplier<? extends EntityType<?>> entityType;
    private final Supplier<? extends Item> containedItem;

    public LocomotiveItem(Properties properties, Supplier<? extends EntityType<?>> entityType, Supplier<? extends Item> containedItem)
    {
        super(properties);
        this.entityType = entityType;
        this.containedItem = containedItem;
    }

    /**
     * Based on {@link net.minecraft.world.item.MinecartItem}
     */
    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = level.getBlockState(pos);
        if (!Helpers.isBlock(state, BlockTags.RAILS))
        {
            return InteractionResult.FAIL;
        }
        else
        {
            ItemStack held = context.getItemInHand();
            if (!level.isClientSide)
            {
                RailShape railshape = state.getBlock() instanceof BaseRailBlock ? ((BaseRailBlock) state.getBlock()).getRailDirection(state, level, pos, null) : RailShape.NORTH_SOUTH;
                double offset = 0.0D;
                if (railshape.isAscending())
                {
                    offset = 0.5D;
                }

                final double x = pos.getX() + 0.5;
                final double y = pos.getY() + 0.0625 + offset;
                final double z = pos.getZ() + 0.5;

                if (createMinecartEntity(level, held, x, y, z))
                {
                    level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            return InteractionResult.PASS;
        }
    }

    public boolean createMinecartEntity(Level level, ItemStack stack, double x, double y, double z)
    {
        final Entity entity = entityType.get().create(level);
        if (entity != null)
        {
            entity.setPos(x, y, z);
            entity.xo = x;
            entity.yo = y;
            entity.zo = z;

            if (stack.hasCustomHoverName())
            {
                entity.setCustomName(stack.getHoverName());
            }

            beforeEntityAdded(level, stack, entity);

            level.addFreshEntity(entity);
            stack.shrink(1);

            return true;
        }
        return false;
    }

    /**
     * Provided for subclasses to manipulate the minecart before it is added to the world
     */
    public void beforeEntityAdded(Level level, ItemStack stack, Entity entity)
    {
        if (entity instanceof LocomotiveEntity engine)
        {
            engine.setPickResult(stack);
            engine.setEngineItem(new ItemStack(containedItem.get()));
        }
    }
}
