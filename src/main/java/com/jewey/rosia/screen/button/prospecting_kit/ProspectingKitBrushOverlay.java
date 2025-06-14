package com.jewey.rosia.screen.button.prospecting_kit;

import com.jewey.rosia.screen.ProspectingKitScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

public class ProspectingKitBrushOverlay extends Button {
    public int step;
    public ProspectingKitBrushOverlay(int step, int guiLeft, int guiTop)
    {
        super(guiLeft + 96, guiTop + 41, 72, 56, Component.nullToEmpty("Overlay"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(step, null));
        }, RenderHelpers.NARRATION);
        this.step = step;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ProspectingKitScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        //render overlay
        if(step < 4)
        {
            pGuiGraphics.blit(ProspectingKitScreen.TEXTURE, getX(), getY(), 176, 19 + (step * 57), 72, 56, 256, 256);
        }
    }
}
