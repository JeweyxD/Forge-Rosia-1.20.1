package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.BoilingCauldronBlockEntity;
import com.jewey.rosia.common.container.BoilingCauldronContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

public class BoilingCauldronScreen extends BlockEntityScreen<BoilingCauldronBlockEntity, BoilingCauldronContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/boiling_cauldron.png");

    public BoilingCauldronScreen(BoilingCauldronContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void init() {
        super.init();
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
        int temp = (int) (51 * blockEntity.getTemperature() / Heat.maxVisibleTemperature());
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

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + 68, topPos + 76 - fillHeight, 16, fillHeight, 16, 16);

                resetToBackgroundSprite();
            }
        });
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
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 67, topPos + 25, 18, 52))
        {
            blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty())
                {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsOf(fluid), mouseX, mouseY);
                }
            });
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int pMouseX, int pMouseY) {
        //Boiling indicator
        if (blockEntity.getProgress() > 0) {
            graphics.drawString(font, "Boiling!", 97, 78, 0x404040, false);
        }
    }
}
