package com.jewey.rosia.screen.button.electric_forge;

import com.jewey.rosia.common.blocks.entity.block_entity.ElectricForgeBlockEntity;
import com.jewey.rosia.screen.ElectricForgeScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;


public class ElectricForgeButtonOff extends Button {
    private final ElectricForgeBlockEntity forge;

    public ElectricForgeButtonOff(ElectricForgeBlockEntity forge, int guiLeft, int guiTop)
    {
        super(guiLeft + 71, guiTop + 87, 34, 9, Component.nullToEmpty("Off"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(2, null));
        }, RenderHelpers.NARRATION);

        this.forge = forge;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ElectricForgeScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.blit(ElectricForgeScreen.TEXTURE, getX(), getY(), 176, 67, width, height, 256, 256);
    }
}
