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

public class ProspectingKitDigOverlay extends Button {
    public int step;
    public ProspectingKitDigOverlay(int step, int guiLeft, int guiTop)
    {
        super(guiLeft + 8, guiTop + 17, 80, 80, Component.nullToEmpty("Overlay"), button -> {
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
        int stepY = 0;
        int stepX = 0;
        switch (step)
        {
            case 0, 1, 2 -> stepX = (step) * 83;
            case 3, 4, 5 -> {
                stepY = 83;
                stepX = (step - 3) * 83;
            }
            case 6, 7, 8 -> {
                stepY = 166;
                stepX = (step - 6) * 83;
            }
        }
        if(step < 9)
        {
            pGuiGraphics.blit(ProspectingKitScreen.OVERLAY, getX(), getY(), stepX, stepY, 80, 80, 256, 256);
        }
    }
}
