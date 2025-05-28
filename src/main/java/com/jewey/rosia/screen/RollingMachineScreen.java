package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.RollingMachineBlockEntity;
import com.jewey.rosia.common.container.RollingMachineContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.energy.IEnergyStorage;

public class RollingMachineScreen extends BlockEntityScreen<RollingMachineBlockEntity, RollingMachineContainer>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/rolling_machine.png");

    public RollingMachineScreen(RollingMachineContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 0;
        imageHeight += 0;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        //render gui
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        //render crafting progress
        if(menu.isCrafting()) {
            graphics.blit(RollingMachineScreen.TEXTURE, leftPos + 85, topPos + 48, 176, 0, menu.getScaledProgress(), 17);
        }

        //render energy gradient
        final IEnergyStorage energy = blockEntity.getEnergyStorage();
        int height = 43;
        int width = 8;
        int X = leftPos  + 156;
        int Y = topPos + 18;
        int stored = (int)(height*(energy.getEnergyStored()/(float)energy.getMaxEnergyStored()));
        if (menu.getBlockEntity().getEnergyStorage().getEnergyStored() > 0)  {
            graphics.fillGradient(X, Y + (height-stored),
                    X + width, Y + height,
                    0xffb51500, 0xff600b00);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderTooltip(graphics, mouseX, mouseY);

        //render energy tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 155, topPos + 17, 10, 45))
        {
            final IEnergyStorage energy = blockEntity.getEnergyStorage();
            final var text = Component.nullToEmpty(energy.getEnergyStored()+"/"+energy.getMaxEnergyStored()+" FE");
            graphics.renderTooltip(font, text, mouseX, mouseY);
        }
    }
}
