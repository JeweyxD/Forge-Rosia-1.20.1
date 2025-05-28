package com.jewey.rosia.common.blocks.block;

import com.jewey.rosia.common.blocks.MultiblockDevice;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.ElectricGrillBlockEntity;
import com.jewey.rosia.common.items.DoubleWideBlockItem;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;
import java.util.function.Supplier;

public class electric_grill extends MultiblockDevice
{
    public electric_grill(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.DROP);
    }

    public Supplier<BlockItem> blockItemSupplier(CreativeModeTab group) {
        return () -> new DoubleWideBlockItem(this, new Item.Properties());
    }

    // Voxel Shape
    private static final VoxelShape SHAPE = Block.box(0,0,0,16,14,16);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        ElectricGrillBlockEntity grill = ModBlockEntities.ELECTRIC_GRILL_BLOCK_ENTITY.get().create(pos, state);
        assert grill != null;
        grill.isDummy = state.getValue(DUMMY);
        return grill;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand)
    {
        if (!state.getValue(ON)) return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.875D;
        double z = pos.getZ() + 0.5D;

        if (rand.nextInt(10) == 0)
        {
            world.playLocalSound(x, y, z, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.6F, false);
        }
        for (int i = 0; i < rand.nextInt(3); i++)
        {
            world.addParticle(ParticleTypes.SMOKE, x + Helpers.triangle(rand), y + rand.nextDouble(), z + Helpers.triangle(rand), 0, 0.005D, 0);
        }
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity)
    {
        if (!entity.fireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity) && world.getBlockState(pos).getValue(ON))
        {
            entity.hurt(entity.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(world, pos, state, entity);
    }

    @Override
    public Direction getDummyOffsetDir(BlockState state) {
        Direction facing = state.getValue(FACING);
        return facing.getClockWise();
    }
}