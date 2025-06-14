package com.jewey.rosia.screen.button.prospecting_kit;

import com.jewey.rosia.screen.ProspectingKitScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.PacketDistributor;

public class ProspectingKitDigButton extends Button {
    public ProspectingKitDigButton(int guiLeft, int guiTop)
    {
        super(guiLeft + 95, guiTop + 16, 36, 18, Component.nullToEmpty("Dig"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(9, null));
        }, RenderHelpers.NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ProspectingKitScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.blit(ProspectingKitScreen.TEXTURE, getX(), getY(), 95, 16, width, height, 256, 256);
    }

    @Override
    public void onPress() {
        super.onPress();
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void playDownSound(SoundManager handler)
    {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.GRAVEL_BREAK, 1.0F));
    }
}
