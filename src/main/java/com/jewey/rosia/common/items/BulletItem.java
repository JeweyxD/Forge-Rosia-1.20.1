package com.jewey.rosia.common.items;

import com.jewey.rosia.common.entities.BulletEntity;
import com.jewey.rosia.common.entities.ModEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BulletItem extends ArrowItem {
    public BulletItem(Properties props) {
        super(props);
    }
    @Override
    public @NotNull AbstractArrow createArrow(Level world, ItemStack ammoStack, LivingEntity shooter) {
        return new BulletEntity(ModEntities.BULLET.get(), shooter, world);
    }
}