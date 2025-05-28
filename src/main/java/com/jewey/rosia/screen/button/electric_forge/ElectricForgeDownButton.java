package com.jewey.rosia.screen.button.electric_forge;

import com.jewey.rosia.common.blocks.entity.block_entity.ElectricForgeBlockEntity;
import com.jewey.rosia.common.container.ElectricForgeContainer;
import com.jewey.rosia.screen.ElectricForgeScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;


public class ElectricForgeDownButton extends Button {
    private final ElectricForgeBlockEntity forge;

    public ElectricForgeDownButton(ElectricForgeBlockEntity forge, int guiLeft, int guiTop)
    {
        super(guiLeft + 43, guiTop + 67, 18, 18, Component.nullToEmpty("Down"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(1, null));
        }, RenderHelpers.NARRATION);

        this.forge = forge;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ElectricForgeScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.blit(ElectricForgeScreen.TEXTURE, getX(), getY(), 176, 29, width, height, 256, 256);
    }
}
