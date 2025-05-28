package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.WaterPumpBlockEntity;
import com.jewey.rosia.common.container.WaterPumpContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;

public class WaterPumpScreen extends BlockEntityScreen<WaterPumpBlockEntity, WaterPumpContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/pump_gui.png");

    public WaterPumpScreen(WaterPumpContainer container, Inventory playerInventory, Component name)
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
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderTooltip(graphics, mouseX, mouseY);

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
    }
}
