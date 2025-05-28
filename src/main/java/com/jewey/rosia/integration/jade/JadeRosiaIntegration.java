package com.jewey.rosia.integration.jade;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import net.dries007.tfc.compat.jade.common.BlockEntityTooltip;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class JadeRosiaIntegration implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registry)
    {
        RosiaBlockEntityTooltips.register((name, tooltip, block) -> register(registry, name, tooltip, block));
    }

    private void register(IWailaClientRegistration registry, ResourceLocation name, BlockEntityTooltip blockEntityTooltip, Class<? extends Block> block)
    {
        registry.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config)
            {
                blockEntityTooltip.display(access.getLevel(), access.getBlockState(), access.getPosition(), access.getBlockEntity(), tooltip::add);
            }

            @Override
            public ResourceLocation getUid()
            {
                return name;
            }
        }, block);
    }
}
