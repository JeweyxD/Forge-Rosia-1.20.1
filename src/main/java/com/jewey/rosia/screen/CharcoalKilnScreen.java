package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.CharcoalKilnBlockEntity;
import com.jewey.rosia.common.container.CharcoalKilnContainer;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CharcoalKilnScreen extends BlockEntityScreen<CharcoalKilnBlockEntity, CharcoalKilnContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/charcoal_kiln_gui.png");

    public CharcoalKilnScreen(CharcoalKilnContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 0;
        imageHeight += 0;
    }
}
