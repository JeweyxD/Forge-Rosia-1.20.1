package com.jewey.rosia.common.entities;

import com.jewey.rosia.Rosia;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

public class ModEntities {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Rosia.MOD_ID);

    public static final RegistryObject<EntityType<BulletEntity>> BULLET = ENTITY_TYPES.register("bullet",
            () -> EntityType.Builder.of((EntityType.EntityFactory<BulletEntity>) BulletEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).build("bullet"));

    public static final RegistryObject<EntityType<LocomotiveEntity>> LOCOMOTIVE = register("locomotive",
            EntityType.Builder.of(LocomotiveEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F).clientTrackingRange(8));



    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder)
    {
        return register(name, builder, true);
    }

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder, boolean serialize)
    {
        final String id = name.toLowerCase(Locale.ROOT);
        return ENTITY_TYPES.register(id, () -> {
            if (!serialize) builder.noSave();
            return builder.build(Rosia.MOD_ID);
        });
    }
}
