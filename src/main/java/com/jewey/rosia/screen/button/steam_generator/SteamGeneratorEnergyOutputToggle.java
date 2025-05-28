package com.jewey.rosia.screen.button.steam_generator;

import com.jewey.rosia.common.blocks.entity.block_entity.SteamGeneratorBlockEntity;
import com.jewey.rosia.screen.SteamGeneratorScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;


public class SteamGeneratorEnergyOutputToggle extends Button {
    private final SteamGeneratorBlockEntity generator;

    public SteamGeneratorEnergyOutputToggle(SteamGeneratorBlockEntity generator, int guiLeft, int guiTop)
    {
        super(guiLeft + 151, guiTop + 90, 18, 12, Component.nullToEmpty("Output Toggle"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
        }, RenderHelpers.NARRATION);
        setTooltip(Tooltip.create(Objects.requireNonNull(Component.nullToEmpty("Toggle Power Output"))));

        this.generator = generator;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SteamGeneratorScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.blit(SteamGeneratorScreen.TEXTURE, getX(), getY(), 176, 59, width, height, 256, 256);
    }
}
