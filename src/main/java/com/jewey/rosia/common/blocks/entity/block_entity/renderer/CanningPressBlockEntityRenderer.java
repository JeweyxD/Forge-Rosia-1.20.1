package com.jewey.rosia.common.blocks.entity.block_entity.renderer;

import com.jewey.rosia.common.blocks.block.canning_press;
import com.jewey.rosia.common.blocks.entity.block_entity.CanningPressBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class CanningPressBlockEntityRenderer implements BlockEntityRenderer<CanningPressBlockEntity> {
    public CanningPressBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(CanningPressBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack itemStack = pBlockEntity.getRenderStack();
        pPoseStack.pushPose();

        // MUST BE IN ORDER OF TRANSLATE -> SCALE -> ROTATE

        switch (pBlockEntity.getBlockState().getValue(canning_press.FACING)) {
            case NORTH -> pPoseStack.translate(0.5f, 0.47f, 0.44f);
            case EAST -> pPoseStack.translate(0.56f, 0.47f, 0.5f);
            case SOUTH -> pPoseStack.translate(0.5f, 0.47f, 0.56f);
            case WEST -> pPoseStack.translate(0.44f, 0.47f, 0.5f);
        }

        pPoseStack.scale(0.4f, 0.4f, 0.4f);

        switch (pBlockEntity.getBlockState().getValue(canning_press.FACING)) {
            case SOUTH -> pPoseStack.mulPose(Axis.YP.rotationDegrees(0));
            case EAST -> pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            case NORTH -> pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
        }

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.GUI, getLightLevel(pBlockEntity.getLevel(),
                        pBlockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 1);
        pPoseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
