package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.container.ProspectingKitContainer;
import com.jewey.rosia.screen.button.prospecting_kit.ProspectingKitBrushButton;
import com.jewey.rosia.screen.button.prospecting_kit.ProspectingKitBrushOverlay;
import com.jewey.rosia.screen.button.prospecting_kit.ProspectingKitDigButton;
import com.jewey.rosia.screen.button.prospecting_kit.ProspectingKitDigOverlay;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProspectingKitScreen extends TFCContainerScreen<ProspectingKitContainer> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/prospecting_kit_gui.png");
    public static final ResourceLocation OVERLAY =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/prospecting_kit_overlay_gui.png");

    public int[] grid;
    public Item[] ore;
    public Item[] uniqueOre;
    public int digStep;
    public int brushStep;

    public ProspectingKitScreen(ProspectingKitContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, TEXTURE);
        inventoryLabelY += 29;
        imageHeight += 29;
        grid = pMenu.getGrid();
        ore = pMenu.getOre();
        uniqueOre = ore;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ProspectingKitDigButton(getGuiLeft(), getGuiTop()));
        addRenderableWidget(new ProspectingKitBrushButton(getGuiLeft(), getGuiTop()));
        for(int step = 8; step >= 0; step--)
        {
            addRenderableWidget(new ProspectingKitDigOverlay(step, getGuiLeft(), getGuiTop()));
        }
        for(int step = 3; step >= 0; step--)
        {
            addRenderableWidget(new ProspectingKitBrushOverlay(step, getGuiLeft(), getGuiTop()));
        }
        this.digStep = 0;
        this.brushStep = 0;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (RenderHelpers.isInside((int) pMouseX, (int) pMouseY, leftPos + 95, topPos + 16, 36, 18))
        {
            this.digStep++;
            for (Renderable widget : renderables)
            {
                if (widget instanceof ProspectingKitDigOverlay overlay)
                {
                    if (overlay.step + 1 == this.digStep)
                    {
                        overlay.visible = false;
                    }
                }
                if (widget instanceof ProspectingKitDigButton button)
                {
                    if (this.digStep == 10)
                    {
                        button.active = false;
                    }
                }
            }
        }
        if (RenderHelpers.isInside((int) pMouseX, (int) pMouseY, leftPos + 131, topPos + 16, 36, 18))
        {
            if(digStep >= 9)
            {
                this.brushStep++;
            }
            for (Renderable widget : renderables)
            {
                if (widget instanceof ProspectingKitBrushOverlay overlay)
                {
                    if (overlay.step + 1 == this.brushStep)
                    {
                        overlay.visible = false;
                    }
                }
                if (widget instanceof ProspectingKitBrushButton button)
                {
                    if(digStep < 9 && button.active)
                    {
                        button.active = false;
                    }
                    if(digStep >= 9 && !button.active)
                    {
                        button.active = true;
                    }
                    if (this.brushStep == 5)
                    {
                        button.active = false;
                    }
                }
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        //render gui
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        renderBackground(pGuiGraphics);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        //render marks
        for(int i = 0; i <= grid.length - 1; i++)
        {
            int stepX = 0;
            int stepY = 0;
            if(i <= 4) {
                stepX = i * 16; }
            else if(i <= 9) {
                stepX = (i - 5) * 16;
                stepY = 16; }
            else if(i <= 14) {
                stepX = (i - 10) * 16;
                stepY = 32; }
            else if(i <= 19) {
                stepX = (i - 15) * 16;
                stepY = 48; }
            else if(i <= 24) {
                stepX = (i - 20) * 16;
                stepY = 64; }
            if(grid[i] == 1) {
                pGuiGraphics.blit(TEXTURE, leftPos + 8 + stepX, topPos + 17 + stepY, 160 + (grid[i] * 17), 1, 16, 16);
            }
        }
        //render ore items
        Item[] items = uniqueOre;
        int[] amount = new int[]{0, 0, 0};
        Item slot1 = null;
        Item slot2 = null;
        Item slot3 = null;
        Item A = null;
        Item B = null;
        Item C = null;
        for (Item item : items) {
            //identify unique ores
            if (item == null) {
                continue;
            }
            if (slot1 == null) {
                slot1 = item;
            }
            if (item == slot1) {
                amount[0]++;
            }
            if (item != slot1 && slot2 == null) {
                slot2 = item;
            }
            if (item == slot2) {
                amount[1]++;
            }
            if (item != slot1 && item != slot2 && slot3 == null) {
                slot3 = item;
            }
            if (item == slot3) {
                amount[2]++;
            }
            //sort by largest
            if (amount[0] >= amount[1] && amount[0] >= amount[2]) {
                A = slot1;
                if (amount[1] >= amount[2]) {
                    B = slot2;
                    C = slot3;
                }
                else {
                    B = slot3;
                    C = slot2;
                }
            }
            if (amount[1] > amount[0] && amount[1] > amount[2]) {
                A = slot2;
                if (amount[0] >= amount[2]) {
                    B = slot1;
                    C = slot3;
                }
                else {
                    B = slot3;
                    C = slot1;
                }
            }
            if (amount[2] > amount[0] && amount[2] > amount[1]) {
                A = slot3;
                if (amount[0] >= amount[1]) {
                    B = slot1;
                    C = slot2;
                }
                else {
                    B = slot2;
                    C = slot1;
                }
            }
        }
        int[] amountDesc = Arrays.stream(amount).boxed()
                .sorted(Collections.reverseOrder())
                .mapToInt(Integer::intValue)
                .toArray();
        Item[] items1 = new Item[]{A, B, C};
        List<Item> uniqueItems = Arrays.asList(items1);
        for(int i = 0; i < 3; i++)
        {
            Item item = uniqueItems.get(i);
            if(item == null)
            {
                continue;
            }
            pGuiGraphics.renderFakeItem(item.getDefaultInstance(), leftPos + 98, topPos + 43 + (i * 18));
            int percent = (int) (((float) amountDesc[i] / 25) * 100);
            Component text = Component.nullToEmpty(percent + "%");
            pGuiGraphics.drawString(font, text, leftPos + 120, topPos + 48 + (i * 18), 0x404040, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int mouseX, int mouseY)
    {
        super.renderTooltip(pGuiGraphics, mouseX, mouseY);
    }
}
