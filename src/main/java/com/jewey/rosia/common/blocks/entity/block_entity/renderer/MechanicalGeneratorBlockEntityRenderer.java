package com.jewey.rosia.common.blocks.entity.block_entity.renderer;

import com.jewey.rosia.common.blocks.block.mechanical_generator;
import com.jewey.rosia.common.blocks.entity.block_entity.MechanicalGeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blocks.rotation.ConnectedAxleBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalGeneratorBlockEntityRenderer implements BlockEntityRenderer<MechanicalGeneratorBlockEntity> {
    @Override
    public void render(MechanicalGeneratorBlockEntity generator, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        final Level level = generator.getLevel();
        final BlockState state = generator.getBlockState();

        if (!(state.getBlock() instanceof mechanical_generator) || level == null)
        {
            return;
        }
        final Direction face = state.getValue(mechanical_generator.FACING);

        final boolean isConnectedToNetwork = generator.isConnectedToNetwork();
        final float rotationAngle = generator.getRotationAngle(partialTicks);

        // Render an extension of the axle
        if (isConnectedToNetwork && level.getBlockState(generator.getBlockPos().relative(face)).getBlock() instanceof ConnectedAxleBlock axleBlock
                && level.getBlockEntity(generator.getBlockPos()) != null)
        {
            final VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());
            final TextureAtlasSprite sprite = RenderHelpers.blockTexture(axleBlock.getAxleTextureLocation());

            stack.pushPose();
            stack.translate(0.5f, 0.5f, 0.5f);
            switch (level.getBlockState(generator.getBlockPos()).getValue(mechanical_generator.FACING)) {
                case SOUTH -> stack.mulPose(Axis.YP.rotationDegrees(180));
                case EAST -> stack.mulPose(Axis.YP.rotationDegrees(270));
                case NORTH -> stack.mulPose(Axis.YP.rotationDegrees(0));
                case WEST -> stack.mulPose(Axis.YP.rotationDegrees(90));
            }
            switch (level.getBlockState(generator.getBlockPos()).getValue(mechanical_generator.FACING)) {
                case SOUTH, EAST -> stack.mulPose(Axis.ZN.rotation(-rotationAngle));
                case NORTH, WEST -> stack.mulPose(Axis.ZN.rotation(rotationAngle));
            }
            stack.translate(-0.5f, -0.5f, -0.5f);

            RenderHelpers.renderTexturedCuboid(stack, buffer, sprite, packedLight, packedOverlay, 6f / 16f, 6f / 16f, 0f, 10f / 16f, 10f / 16f, 0.5f, false);

            stack.popPose();
        }
    }
}
