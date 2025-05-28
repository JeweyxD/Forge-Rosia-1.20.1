package com.jewey.rosia.screen.button.lava_basin;

import com.jewey.rosia.common.blocks.entity.block_entity.LavaBasinBlockEntity;
import com.jewey.rosia.screen.LavaBasinScreen;
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


public class LavaBasinSlag extends Button {
    private final LavaBasinBlockEntity entity;

    public LavaBasinSlag(LavaBasinBlockEntity entity, int guiLeft, int guiTop)
    {
        super(guiLeft + 97, guiTop + 59, 18, 18, Component.nullToEmpty("Slag"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
        }, RenderHelpers.NARRATION);
        setTooltip(Tooltip.create(Objects.requireNonNull(Component.nullToEmpty("Empty Slag"))));

        this.entity = entity;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, LavaBasinScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.blit(LavaBasinScreen.TEXTURE, getX(), getY(), 176, 8, width, height, 256, 256);
    }
}
