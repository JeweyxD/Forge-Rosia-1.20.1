package com.jewey.rosia.event;

import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.blocks.entity.block_entity.CharcoalKilnBlockEntity;
import com.jewey.rosia.common.blocks.entity.block_entity.FireBoxBlockEntity;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ForgeEventHandler {
    public static void init()
    {
        final IEventBus bus = MinecraftForge.EVENT_BUS;

        bus.addListener(ForgeEventHandler::onFireStart);
    }

    public static void onFireStart(StartFireEvent event)
    {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();

        if (block == ModBlocks.CHARCOAL_KILN.get() && event.isStrong())
        {
            final BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof CharcoalKilnBlockEntity kiln && kiln.light(state, kiln))
            {
                event.setCanceled(true);
            }
        }
        else if (block == ModBlocks.FIRE_BOX.get() && event.isStrong())
        {
            final BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof FireBoxBlockEntity firebox && firebox.light(state, firebox))
            {
                event.setCanceled(true);
            }
        }
    }
}
