package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.SteamGeneratorBlockEntity;
import com.jewey.rosia.common.container.SteamGeneratorContainer;
import com.jewey.rosia.screen.button.steam_generator.SteamGeneratorEnergyOutputToggle;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;

public class SteamGeneratorScreen extends BlockEntityScreen<SteamGeneratorBlockEntity, SteamGeneratorContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/steam_generator_gui.png");

    public SteamGeneratorScreen(SteamGeneratorContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new SteamGeneratorEnergyOutputToggle(blockEntity, getGuiLeft(), getGuiTop()));
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

        //render temp indicator
        int temp = (int) (51 * blockEntity.getTemperature() / 2014);    // 2014 -> temp for max FE/tick
        if (temp > 0)
        {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 76 - Math.min(51, temp), 176, 0, 15, 5);
        }

        //render fluid
        blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (!fluidStack.isEmpty())
            {
                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) 50 * fluidStack.getAmount() / (float) fluidHandler.getTankCapacity(0));

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + 67, topPos + 76 - fillHeight, 16, fillHeight, 16, 16);

                resetToBackgroundSprite();
            }
        });

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

        //render meter
        int power = 0;
        if(!blockEntity.getFluidStorage().getFluidInTank(0).isEmpty()) {
            if (blockEntity.getTemperature() >= 1951) {power = 5;}
            else if (blockEntity.getTemperature() >= 1415) {power = 4;}
            else if (blockEntity.getTemperature() >= 1350) {power = 3;}
            else if (blockEntity.getTemperature() >= 1100) {power = 2;}
            else if (blockEntity.getTemperature() >= 100) {power = 1;}
        }
        int tempOffset = power * 31;
        graphics.blit(TEXTURE, leftPos+ 100, topPos + 54, tempOffset, 188, 30, 22);
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

        //render fluid tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 66, topPos + 25, 18, 52))
        {
            blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty())
                {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsOf(fluid), mouseX, mouseY);
                }
            });
        }

        //render energy tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 155, topPos + 25, 10, 52))
        {
            final IEnergyStorage energy = blockEntity.getEnergyStorage();
            final var text = Component.nullToEmpty(energy.getEnergyStored()+"/"+energy.getMaxEnergyStored()+" FE");
            graphics.renderTooltip(font, text, mouseX, mouseY);
        }

        //render FE/tick tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 100, topPos + 54, 30, 22)
                && blockEntity.getTemperature() >= 100)
        {
            int power = 0;
            Component text;
            if (blockEntity.getTemperature() >= 1951) {power = 5;}
            else if (blockEntity.getTemperature() >= 1415) {power = 4;}
            else if (blockEntity.getTemperature() >= 1350) {power = 3;}
            else if (blockEntity.getTemperature() >= 1100) {power = 2;}
            else if(blockEntity.getTemperature() >= 100) {power = 1;}
            if(!blockEntity.getFluidStorage().getFluidInTank(0).isEmpty()) {
                if (blockEntity.getTemperature() >= 650) {
                    text = Component.nullToEmpty(power + " FE/Tick");
                } else {
                    text = Component.nullToEmpty("< " + power + " FE/Tick");
                }
            } else  {
               text = Component.nullToEmpty("No Water").copy().withStyle(ChatFormatting.RED);
            }

            graphics.renderTooltip(font, text, mouseX, mouseY);
        }
    }
}
