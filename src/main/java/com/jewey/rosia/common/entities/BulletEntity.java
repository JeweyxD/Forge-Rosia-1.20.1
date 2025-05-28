package com.jewey.rosia.common.entities;

import com.jewey.rosia.common.items.ModItems;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

public class BulletEntity extends AbstractArrow {
    private double baseDamage = 10.0D;

    public void setBaseDamage(double pBaseDamage) {
        this.baseDamage = pBaseDamage;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }
    public BulletEntity(EntityType<BulletEntity> entityType, Level world) {
        super(entityType, world);
    }

    public BulletEntity(EntityType<BulletEntity> entityType, double x, double y, double z, Level world) {
        super(entityType, x, y, z, world);
    }

    public BulletEntity(EntityType<BulletEntity> entityType, LivingEntity shooter, Level world) {
        super(entityType, shooter, world);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.BULLET.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult ray) {
        super.onHitEntity(ray);
    }


    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
