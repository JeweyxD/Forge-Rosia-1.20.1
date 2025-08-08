package com.jewey.rosia.event;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.renderer.*;
import com.jewey.rosia.common.entities.ModEntities;
import com.jewey.rosia.common.items.ModItems;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.items.TFCFishingRodItem;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


public class ModClientEvents {

    public static void init()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ModClientEvents::registerRenderers);
        bus.addListener(ModClientEvents::clientSetup);
    }

    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CANNING_PRESS_BLOCK_ENTITY.get(), CanningPressBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BOILING_CAULDRON_BLOCK_ENTITY.get(), ctx -> new BoilingCauldronBlockEntityRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.MECHANICAL_GENERATOR_BLOCK_ENTITY.get(), ctx -> new MechanicalGeneratorBlockEntityRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.ELECTRIC_GRILL_BLOCK_ENTITY.get(), ctx -> new ElectricGrillBlockEntityRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.COOLING_BASIN_BLOCK_ENTITY.get(), ctx -> new CoolingBasinBlockEntityRenderer());

        event.registerEntityRenderer(ModEntities.LOCOMOTIVE.get(), ctx -> new MinecartRenderer<>(ctx, RenderHelpers.modelIdentifier("chest_minecart")));
    }

    public static void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            Item rod = ModItems.PURPLE_STEEL_FISHING_ROD.get();
            ItemProperties.register(rod, Helpers.identifier("cast"), (stack, level, entity, unused) -> {
                if (entity == null)
                {
                    return 0.0F;
                }
                else
                {
                    return entity instanceof Player player && TFCFishingRodItem.isThisTheHeldRod(player, stack) && player.fishing != null ? 1.0F : 0.0F;
                }
            });

            Item shield = ModItems.PURPLE_STEEL_SHIELD.get();
            ItemProperties.register(shield, new ResourceLocation("blocking"), (stack, level, entity, unused) -> {
                if (entity == null)
                {
                    return 0.0F;
                }
                else
                {
                    return entity instanceof Player && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f;
                }
            });

            Item javelin = ModItems.PURPLE_STEEL_JAVELIN.get();
            ItemProperties.register(javelin, Helpers.identifier("throwing"), (stack, level, entity, unused) ->
                    entity != null && ((entity.isUsingItem() && entity.getUseItem() == stack) || (entity instanceof Monster monster && monster.isAggressive())) ? 1.0F : 0.0F
            );
        });
    }
}
