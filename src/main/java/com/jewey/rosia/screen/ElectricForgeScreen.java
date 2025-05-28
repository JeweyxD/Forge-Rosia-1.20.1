package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.ElectricForgeBlockEntity;
import com.jewey.rosia.common.container.ElectricForgeContainer;
import com.jewey.rosia.screen.button.electric_forge.ElectricForgeButtonOff;
import com.jewey.rosia.screen.button.electric_forge.ElectricForgeDownButton;
import com.jewey.rosia.screen.button.electric_forge.ElectricForgeUpButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.energy.IEnergyStorage;

public class ElectricForgeScreen extends BlockEntityScreen<ElectricForgeBlockEntity, ElectricForgeContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/electric_forge_gui.png");

    public ElectricForgeScreen(ElectricForgeContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ElectricForgeDownButton(blockEntity, getGuiLeft(), getGuiTop()));
        addRenderableWidget(new ElectricForgeUpButton(blockEntity, getGuiLeft(), getGuiTop()));
        addRenderableWidget(new ElectricForgeButtonOff(blockEntity, getGuiLeft(), getGuiTop()));
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

        //render target temp
        int setTemp = (int) (51 * blockEntity.getBurnTemperature() / 1500);
        if (setTemp > 0)
        {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 76 - Math.min(51, setTemp), 176, 6, 15, 5);
            graphics.blit(TEXTURE, leftPos + 61 + Math.min(49, setTemp), topPos + 68, 176, 12, 5, 16);
        }
        //render actual temp
        int temp = (int) (51 * blockEntity.getTemperature() / 1500);
        if (temp > 0)
        {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 76 - Math.min(51, temp), 176, 0, 15, 5);
        }

        //render energy gradient
        final IEnergyStorage energy = blockEntity.getEnergyStorage();
        int height = 50;
        int width = 8;
        int X = leftPos  + 156;
        int Y = topPos + 26;
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

        //render temp tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 8, topPos + 25, 15, 52))
        {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null)
            {
                graphics.renderTooltip(font, text, mouseX, mouseY);
            }
        }
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 43, topPos + 71, 90, 10))
        {
            if (blockEntity.getBurnTemperature() != 0) {
                var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getBurnTemperature());
                int temp = (int) blockEntity.getBurnTemperature();
                final var text2 = Component.nullToEmpty(temp + "° ").copy().append(text);

                graphics.renderTooltip(font, text2, mouseX, mouseY);
            }
        }

        //render energy tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 155, topPos + 25, 10, 52))
        {
            final IEnergyStorage energy = blockEntity.getEnergyStorage();
            final var text = Component.nullToEmpty(energy.getEnergyStored()+"/"+energy.getMaxEnergyStored()+" FE");
            graphics.renderTooltip(font, text, mouseX, mouseY);
        }

        //render button tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 43, topPos + 67, 18, 18))
        {
            Component text = null;
            if(!Screen.hasShiftDown()){
                text = Component.nullToEmpty("-10°");
            }
            else if(Screen.hasShiftDown()){
                text = Component.nullToEmpty("-100°");
            }
            assert text != null;
            graphics.renderTooltip(font, text, mouseX, mouseY + 16);
        }
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 115, topPos + 67, 18, 18))
        {
            Component text = null;
            if(!Screen.hasShiftDown()){
                text = Component.nullToEmpty("+10°");
            }
            else if(Screen.hasShiftDown()){
                text = Component.nullToEmpty("+100°");
            }
            assert text != null;
            graphics.renderTooltip(font, text, mouseX, mouseY + 16);
        }
    }
}
