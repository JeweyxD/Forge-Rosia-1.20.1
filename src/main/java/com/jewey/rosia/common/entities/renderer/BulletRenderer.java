package com.jewey.rosia.common.entities.renderer;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.entities.BulletEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BulletRenderer extends ArrowRenderer<BulletEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Rosia.MOD_ID, "textures/entity/projectiles/bullet_entity.png");

    public BulletRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    public ResourceLocation getTextureLocation(BulletEntity arrow) {
        return TEXTURE;
    }
}
