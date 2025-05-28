package com.jewey.rosia.screen.button.zinc_silver_battery;

import com.jewey.rosia.common.blocks.entity.block_entity.ZincSilverBatteryBlockEntity;
import com.jewey.rosia.screen.ZincSilverBatteryScreen;
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


public class ZincSilverBatteryEnergyOutputToggle extends Button {
    private final ZincSilverBatteryBlockEntity battery;

    public ZincSilverBatteryEnergyOutputToggle(ZincSilverBatteryBlockEntity battery, int guiLeft, int guiTop)
    {
        super(guiLeft + 151, guiTop + 70, 18, 12, Component.nullToEmpty("Output Toggle"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
        }, RenderHelpers.NARRATION);
        setTooltip(Tooltip.create(Objects.requireNonNull(Component.nullToEmpty("Toggle Power Output"))));

        this.battery = battery;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ZincSilverBatteryScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.blit(ZincSilverBatteryScreen.TEXTURE, getX(), getY(), 177, 0, width, height, 256, 256);
    }
}
