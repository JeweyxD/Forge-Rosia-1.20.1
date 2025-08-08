package com.jewey.rosia.common.blocks.entity.block_entity.renderer;

import com.jewey.rosia.common.blocks.entity.block_entity.CoolingBasinBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class CoolingBasinBlockEntityRenderer implements BlockEntityRenderer<CoolingBasinBlockEntity>
{
    @Override
    public void render(CoolingBasinBlockEntity basin, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
    {
        //Render fluid in world
        basin.getCapability(Capabilities.FLUID).map(handler -> handler.getFluidInTank(0)).filter(fluid -> !fluid.isEmpty()).ifPresent(fluidStack -> {
            final float fillPercent = (float) fluidStack.getAmount() / basin.getFluidStorage().getTankCapacity(0);
            final float renderPercent = fillPercent > 0 ? Math.max(fillPercent, 0.1615F) : 0;
            final float subtract = fillPercent > 0.03 ? 0f : (0.03f - fillPercent) * 7f;
            RenderHelpers.renderFluidFace(poseStack, fluidStack, buffer, 0.125F + subtract, 0.125F + subtract, 0.875F - subtract, 0.875F - subtract, 0.8125F * renderPercent, combinedOverlay, combinedLight);
        });
    }
}
