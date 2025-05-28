package com.jewey.rosia.common.entities;

import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.container.LocomotiveContainer;
import net.dries007.tfc.util.Fuel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class LocomotiveEntity extends MinecartChest implements HasCustomInventoryScreen {
    public static final EntityDataAccessor<ItemStack> DATA_CART_ITEM =
            SynchedEntityData.defineId(LocomotiveEntity.class, EntityDataSerializers.ITEM_STACK);
    public static final EntityDataAccessor<ItemStack> DATA_ENGINE_ITEM =
            SynchedEntityData.defineId(LocomotiveEntity.class, EntityDataSerializers.ITEM_STACK);


    private int burnTicks = 0;

    public LocomotiveEntity(EntityType<? extends MinecartChest> entityType, Level level)
    {
        super(entityType, level);
    }

    public float getBurnTicks() {
        return burnTicks;
    }

    @Override
    public int getContainerSize()
    {
        return 12;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.put("cartItem", getPickResult().save(new CompoundTag()));
        tag.put("engineItem", getChestItem().save(new CompoundTag()));
        tag.putInt("burnTicks", burnTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        setPickResult(ItemStack.of(tag.getCompound("cartItem")));
        setEngineItem(ItemStack.of(tag.getCompound("engineItem")));
        burnTicks = tag.getInt("burnTicks");
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_ENGINE_ITEM, ItemStack.EMPTY);
        entityData.define(DATA_CART_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getChestItem()
    {
        return entityData.get(DATA_ENGINE_ITEM);
    }

    public void setEngineItem(ItemStack item)
    {
        entityData.set(DATA_ENGINE_ITEM, item.copy());
    }

    public void setPickResult(ItemStack item)
    {
        entityData.set(DATA_CART_ITEM, item.copy());
    }

    @Override
    @NotNull
    public ItemStack getPickResult()
    {
        return entityData.get(DATA_CART_ITEM).copy();
    }


    public AbstractContainerMenu createMenu(int windowId, Inventory inventory)
    {
        return LocomotiveContainer.createMenu(windowId, inventory, this);
    }

    @Override
    public @NotNull BlockState getDisplayBlockState()
    {
        return getChestItem().getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public @NotNull BlockState getDefaultDisplayBlockState() {
        return ModBlocks.STIRLING_ENGINE_SLAB.get().defaultBlockState();
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.3D;
    }

    @Override
    public boolean hasCustomDisplay()
    {
        return true; // tells vanilla to render getDisplayBlockState
    }

    @Override
    protected Item getDropItem()
    {
        return getPickResult().getItem();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        if (this.canAddPassenger(pPlayer) && !pPlayer.isSecondaryUseActive()) {
            if (pPlayer.isSecondaryUseActive()) {
                return InteractionResult.PASS;
            } else if (!this.level().isClientSide) {
                return pPlayer.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            } else {
                return InteractionResult.SUCCESS;
            }
        } else {
            InteractionResult interactionresult = this.interactWithContainerVehicle(pPlayer);
            if (interactionresult.consumesAction()) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
                PiglinAi.angerNearbyPiglins(pPlayer, true);
            }

            return interactionresult;
        }
    }

    @Override
    protected boolean canAddPassenger(Entity pPassenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return true;
    }

    @Override
    public void openCustomInventoryScreen(Player pPlayer) {
        pPlayer.openMenu(this);
        if (!pPlayer.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
        }
    }

    protected void moveAlongTrack(BlockPos pPos, BlockState pState) {
        super.moveAlongTrack(pPos, pState);
        double delta = burnTicks > 0 ? 2D : 0.1D;
        boolean flag1 = false;
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player) {
            if (this.burnTicks == 0) {
                Vec3 vec32 = entity.getDeltaMovement();
                double d9 = vec32.horizontalDistanceSqr();
                double d11 = this.getDeltaMovement().horizontalDistanceSqr();
                if (d9 > 1.0E-4D && d11 < getMaxSpeed()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(vec32.x * delta, 0.0D, vec32.z * delta));
                    flag1 = false;
                }
            } else {
                Vec3 vec32 = this.getDeltaMovement();
                double d9 = vec32.horizontalDistance();
                if (d9 > 0.01D) {
                    this.setDeltaMovement(vec32.add(vec32.x / d9 * delta, 0.0D, vec32.z / d9 * delta));
                    flag1 = false;
                }
            }
        }
    }

    public void tick() {
        super.tick();
        //Only consume fuel if the locomotive is moving
        if (burnTicks > 0 && isMoving(0.01D)) {
            burnTicks--;
        }
        ItemStack fuelStack = getItem(9);
        ItemStack fuelStack2 = getItem(10);
        ItemStack fuelStack3 = getItem(11);
        if (burnTicks <= 0 && !fuelStack.isEmpty()) {
            consumeFuel();
        }
        if ((fuelStack.isEmpty() && !fuelStack2.isEmpty()) || (fuelStack2.isEmpty() && !fuelStack3.isEmpty())) {
            cascadeFuelSlots();
        }
        if (this.random.nextInt(4) == 0 && this.isMoving(2.5D)) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    protected boolean isMoving(double delta)
    {
        return (this.getDeltaMovement().x > delta || this.getDeltaMovement().z > delta)
                || (this.getDeltaMovement().x < -delta || this.getDeltaMovement().z < -delta);
    }

    private void consumeFuel()
    {
        final ItemStack fuelStack = getItem(9);
        if (!fuelStack.isEmpty())
        {
            // Try and consume a piece of fuel
            Fuel fuel = Fuel.get(fuelStack);
            if (fuel != null) {
                if (fuel.getDuration() > 0) {
                    removeItem(9, 1);
                    burnTicks += fuel.getDuration();
                }
            }
        }
    }

    private void cascadeFuelSlots() {
        // move stack from slot 10 to 9 if empty
        if (getItem(9).isEmpty() && !getItem(10).isEmpty())
        {
            setItem(9, getItem(10).copy());
            setItem(10, ItemStack.EMPTY);
        }
        // move stack from slot 11 to 10 if empty
        else if (getItem(10).isEmpty() && !getItem(11).isEmpty())
        {
            setItem(10, getItem(11).copy());
            setItem(11, ItemStack.EMPTY);
        }
    }
}
