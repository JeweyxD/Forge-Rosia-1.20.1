package com.jewey.rosia.common.blocks.block;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.LavaBasinBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Random;

public class lava_basin extends DeviceBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof LavaBasinBlockEntity) {
                ((LavaBasinBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    public lava_basin(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any().setValue(LIT, false));
    }
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand)
    {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.875D;
        double z = pos.getZ() + 0.5D;

        if (rand.nextInt(8) == 1)
        {
            world.playLocalSound(x, y, z, SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.15F, rand.nextFloat() * 0.7F + 0.6F, false);
        }
        for (int i = 0; i < rand.nextInt(3); i++)
        {
            world.addParticle(ParticleTypes.SMOKE, x + Helpers.triangle(rand), y + rand.nextDouble(), z + Helpers.triangle(rand), 0, 0.005D, 0);
        }
        if (rand.nextInt(8) == 1)
        {
            world.addParticle(ParticleTypes.LAVA, x + Helpers.triangle(rand), y + rand.nextDouble(), z + Helpers.triangle(rand), 0, 0.005D, 0);
        }
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity)
    {
        if (!entity.fireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity) && world.getBlockState(pos).getValue(LIT))
        {
            entity.hurt(entity.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(world, pos, state, entity);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
        builder.add(LIT);
    }
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        LavaBasinBlockEntity basin = level.getBlockEntity(pos, ModBlockEntities.LAVA_BASIN_BLOCK_ENTITY.get()).orElse(null);
        if (basin != null)
        {
            //Bucket/fluid-inventory interaction i.e. place fluid from hand into device
            final ItemStack stack = player.getItemInHand(hand);
            if (FluidHelpers.transferBetweenBlockEntityAndItem(stack, basin, player, hand))
            {
            return InteractionResult.SUCCESS;
            }

            else if (player instanceof ServerPlayer serverPlayer)
            {
                Helpers.openScreen(serverPlayer, basin, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
