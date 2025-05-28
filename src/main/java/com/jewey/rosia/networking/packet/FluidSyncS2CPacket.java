package com.jewey.rosia.networking.packet;

import com.jewey.rosia.common.blocks.entity.block_entity.BoilingCauldronBlockEntity;
import com.jewey.rosia.common.blocks.entity.block_entity.LavaBasinBlockEntity;
import com.jewey.rosia.common.blocks.entity.block_entity.SteamGeneratorBlockEntity;
import com.jewey.rosia.common.blocks.entity.block_entity.WaterPumpBlockEntity;
import com.jewey.rosia.common.container.BoilingCauldronContainer;
import com.jewey.rosia.common.container.LavaBasinContainer;
import com.jewey.rosia.common.container.SteamGeneratorContainer;
import com.jewey.rosia.common.container.WaterPumpContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FluidSyncS2CPacket {
    private final FluidStack fluidStack;
    private final BlockPos pos;

    public FluidSyncS2CPacket(FluidStack fluidStack, BlockPos pos) {
        this.fluidStack = fluidStack;
        this.pos = pos;
    }

    public FluidSyncS2CPacket(FriendlyByteBuf buf) {
        this.fluidStack = buf.readFluidStack();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFluidStack(fluidStack);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof SteamGeneratorBlockEntity blockEntity) {
                blockEntity.setFluid(this.fluidStack);

                if(Minecraft.getInstance().player.containerMenu instanceof SteamGeneratorContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    menu.setFluid(this.fluidStack);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof WaterPumpBlockEntity blockEntity) {
                blockEntity.setFluid(this.fluidStack);

                if(Minecraft.getInstance().player.containerMenu instanceof WaterPumpContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    menu.setFluid(this.fluidStack);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof BoilingCauldronBlockEntity blockEntity) {
                blockEntity.setFluid(this.fluidStack);

                if(Minecraft.getInstance().player.containerMenu instanceof BoilingCauldronContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    menu.setFluid(this.fluidStack);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof LavaBasinBlockEntity blockEntity) {
                blockEntity.setFluid(this.fluidStack);

                if(Minecraft.getInstance().player.containerMenu instanceof LavaBasinContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    menu.setFluid(this.fluidStack);
                }
            }
        });
        return true;
    }
}