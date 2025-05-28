package com.jewey.rosia.common.blocks.block;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.NickelIronBatteryBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.energy.EnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class nickel_iron_battery extends DeviceBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public nickel_iron_battery(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.SAVE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Voxel Shape
    private static final VoxelShape SHAPE = Block.box(4,0,4,12,16,12);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        NickelIronBatteryBlockEntity entity = level.getBlockEntity(pos, ModBlockEntities.NICKEL_IRON_BATTERY_BLOCK_ENTITY.get()).orElse(null);
        if (entity != null)
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                Helpers.openScreen(serverPlayer, entity, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> components, TooltipFlag pFlag) {
        final CompoundTag tag = pStack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
        if(pStack.hasTag() && tag != null){
            int energy = tag.getInt("energy");
            components.add(Component.nullToEmpty(energy + "/2000 FE").copy().withStyle(ChatFormatting.GREEN));
        }
        else {
            components.add(Component.nullToEmpty("0/2000 FE").copy().withStyle(ChatFormatting.GREEN));
        }
        super.appendHoverText(pStack, pLevel, components, pFlag);
    }
}
