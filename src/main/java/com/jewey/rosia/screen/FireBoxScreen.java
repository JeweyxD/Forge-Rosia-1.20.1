package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.FireBoxBlockEntity;
import com.jewey.rosia.common.container.FireBoxContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FireBoxScreen extends BlockEntityScreen<FireBoxBlockEntity, FireBoxContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/firebox_gui.png");

    public FireBoxScreen(FireBoxContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        //render gui
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        //render temp indicator
        int temp = (int) (51 * blockEntity.getTemperature() / Heat.maxVisibleTemperature());
        if (temp > 0)
        {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 76 - Math.min(51, temp), 176, 0, 15, 5);
        }

        //render burn progress
        float getBurnProgress = (blockEntity.getBurnTicks() / blockEntity.getBurnTicksInit()) * 15;
        graphics.blit(TEXTURE, leftPos + 78, topPos + 50 - (int) getBurnProgress, 176, 20 - (int) getBurnProgress, 19, (int) getBurnProgress);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderTooltip(graphics, mouseX, mouseY);

        //render temp tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 8, topPos + 25, 15, 52))
        {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null)
            {
                graphics.renderTooltip(font, text, mouseX, mouseY);
            }
        }
    }
}
