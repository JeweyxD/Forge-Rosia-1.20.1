package com.jewey.rosia.common.blocks.entity.block_entity.renderer;

import com.jewey.rosia.common.blocks.block.electric_grill;
import com.jewey.rosia.common.blocks.entity.block_entity.ElectricGrillBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ElectricGrillBlockEntityRenderer  implements BlockEntityRenderer<ElectricGrillBlockEntity>
{
    @Override
    public void render(ElectricGrillBlockEntity grill, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
    {
        grill.getCapability(Capabilities.ITEM).ifPresent(cap -> {
            for (int i = 0; i <= 9; i++)
            {
                final ItemStack item = cap.getStackInSlot(i);
                if (!item.isEmpty())
                {
                    poseStack.pushPose();
                    switch (grill.getBlockState().getValue(electric_grill.FACING)) {
                        case NORTH -> {
                            poseStack.translate(0.25F, 0.890625F, 0.3125F);
                            if(i < 5){poseStack.translate(0.375F * i, 0F, 0F);}
                            if(i > 4){poseStack.translate(0.375F * (i - 5), 0F, 0.375F);}
                            poseStack.scale(0.3F, 0.3F, 0.3F);
                            poseStack.mulPose(Axis.YP.rotationDegrees(0));
                        }
                        case WEST -> {
                            poseStack.translate(0.3125F, 0.890625F, 0.75F);
                            if(i < 5){poseStack.translate(0F, 0F, -0.375F * i);}
                            if(i > 4){poseStack.translate(0.375F, 0F, -0.375F * (i - 5));}
                            poseStack.scale(0.3F, 0.3F, 0.3F);
                            poseStack.mulPose(Axis.YP.rotationDegrees(90));
                        }
                        case SOUTH -> {
                            poseStack.translate(0.75F, 0.890625F, 0.6875F);
                            if(i < 5){poseStack.translate(-0.375F * i, 0F, 0F);}
                            if(i > 4){poseStack.translate(-0.375F * (i - 5), 0F, -0.375F);}
                            poseStack.scale(0.3F, 0.3F, 0.3F);
                            poseStack.mulPose(Axis.YP.rotationDegrees(180));
                        }
                        case EAST -> {
                            poseStack.translate(0.6875F, 0.890625F, 0.25F);
                            if(i < 5){poseStack.translate(0F, 0F, 0.375F * i);}
                            if(i > 4){poseStack.translate(-0.375F, 0F, 0.375F * (i - 5));}
                            poseStack.scale(0.3F, 0.3F, 0.3F);
                            poseStack.mulPose(Axis.YP.rotationDegrees(270));
                        }
                    }
                    poseStack.mulPose(Axis.XP.rotationDegrees(90f));

                    Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, grill.getLevel(), 0);

                    poseStack.popPose();
                }
            }
        });
    }
}
