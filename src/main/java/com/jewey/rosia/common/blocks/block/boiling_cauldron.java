package com.jewey.rosia.common.blocks.block;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.BoilingCauldronBlockEntity;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.Random;

public class boiling_cauldron extends DeviceBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public static VoxelShape makeShape(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.1875, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0, 0.125, 0.875, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.125, 0.8125, 1, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.8125, 0.8125, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.125, 0.8125), BooleanOp.OR);
        return shape;
    }

    private static final VoxelShape SHAPE = makeShape();

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof BoilingCauldronBlockEntity) {
                ((BoilingCauldronBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    public boiling_cauldron(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any().setValue(LIT, false));
    }
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand)
    {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        var entity = Objects.requireNonNull(ModBlockEntities.BOILING_CAULDRON_BLOCK_ENTITY.get().getBlockEntity(level, pos)).getFluidStorage();
        final float fillPercent = (float) entity.getFluidInTank(0).getAmount() / (float) entity.getTankCapacity(0);
        for (int i = 0; i < rand.nextInt(5) + 4; i++)
        {
            level.addParticle(TFCParticles.BUBBLE.get(), false, x + rand.nextFloat() * 0.375 - 0.1875, y + (0.9 * fillPercent), z + rand.nextFloat() * 0.375 - 0.1875, 0, 0.05D, 0);
        }
        level.addParticle(TFCParticles.STEAM.get(), false, x, y + (1.05 * fillPercent), z, Helpers.triangle(rand), 0.5, Helpers.triangle(rand));
        level.playLocalSound(x, y, z, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 1.0F, rand.nextFloat() * 0.7F + 0.4F, false);
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
        BoilingCauldronBlockEntity boiler = level.getBlockEntity(pos, ModBlockEntities.BOILING_CAULDRON_BLOCK_ENTITY.get()).orElse(null);
        if (boiler != null)
        {
            //Bucket/fluid-inventory interaction i.e. place fluid from hand into device
            final ItemStack stack = player.getItemInHand(hand);
            if (FluidHelpers.transferBetweenBlockEntityAndItem(stack, boiler, player, hand))
            {
            return InteractionResult.SUCCESS;
            }

            else if (player instanceof ServerPlayer serverPlayer)
            {
                Helpers.openScreen(serverPlayer, boiler, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
