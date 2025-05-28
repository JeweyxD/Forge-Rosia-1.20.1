package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.container.LocomotiveContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LocomotiveScreen extends TFCContainerScreen<LocomotiveContainer>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/locomotive.png");
    public LocomotiveScreen(LocomotiveContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TEXTURE);
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
    }
}
