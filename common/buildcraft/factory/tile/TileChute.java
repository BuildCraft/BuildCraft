/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.block.BlockChute;
import buildcraft.factory.container.ContainerChute;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.inventory.TransactorEntityItem;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TileChute extends TileBC_Neptune implements ITickable, IDebuggable, IBCTileMenuProvider {
    private static final ResourceLocation ADVANCEMENT_DID_INSERT = new ResourceLocation("buildcraftfactory:retired_hopper");

    private static final int PICKUP_MAX = 3;

    public final ItemHandlerSimple inv = itemManager.addInvHandler(
            "inv",
            4,
            EnumAccess.INSERT,
            EnumPipePart.VALUES
    );

    private final MjBattery battery = new MjBattery(1 * MjAPI.MJ);
    private int progress = 0;

    public TileChute() {
        super(BCFactoryBlocks.chuteTile.get());
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    // public static boolean hasInventoryAtPosition(IBlockAccess world, BlockPos pos, Direction side)
    public static boolean hasInventoryAtPosition(IWorld world, BlockPos pos, Direction side) {
        TileEntity tile = world.getBlockEntity(pos);
        return ItemTransactorHelper.getTransactor(tile, side.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    private void pickupItems(Direction currentSide) {
        AxisAlignedBB aabb = BoundingBoxUtil.extrudeFace(getBlockPos(), currentSide, 0.25);
        int count = PICKUP_MAX;
//        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, aabb, EntitySelectors.IS_ALIVE))
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, aabb, EntityPredicates.ENTITY_STILL_ALIVE)) {
            int moved = ItemTransactorHelper.move(new TransactorEntityItem(entity), inv, count);
            count -= moved;
            if (count <= 0) {
                return;
            }
        }
    }

    private void putInNearInventories(Direction currentSide) {
        boolean[] didWork = { false };
//        List<Direction> sides = new ArrayList<>(Arrays.asList(Direction.values()));
        List<Direction> sides = new ArrayList<>(Arrays.asList(Direction.values().clone()));
        Collections.shuffle(sides, new Random());
        sides.removeIf(Predicate.isEqual(currentSide));
        Stream.<Pair<Direction, ICapabilityProvider>>concat(
                        sides.stream()
                                .map(side -> Pair.of(side, level.getBlockEntity(worldPosition.relative(side)))),
                        sides.stream()
                                .flatMap(side ->
                                        level.getEntitiesOfClass(Entity.class, new AxisAlignedBB(worldPosition.relative(side))).stream()
                                                .filter(entity -> !(entity instanceof LivingEntity))
                                                .map(entity -> Pair.of(side, entity))
                                )
                )
                .map(sideProvider -> ItemTransactorHelper.getTransactor(sideProvider.getRight(), sideProvider.getLeft().getOpposite()))
                .filter(Predicate.isEqual(NoSpaceTransactor.INSTANCE).negate())
                .forEach(transactor ->
                {
                    if (ItemTransactorHelper.move(inv, transactor, 1) > 0) {
                        didWork[0] = true;
                    }
                });
        if (didWork[0]) {
            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DID_INSERT);
        }
    }

    // ITickable

    @Override
    public void update() {
        ITickable.super.update();
        if (level.isClientSide) {
            return;
        }

        if (!(level.getBlockState(worldPosition).getBlock() instanceof BlockChute)) {
            return;
        }

        battery.tick(getLevel(), getBlockPos());

        Direction currentSide = level.getBlockState(worldPosition).getValue(BlockBCBase_Neptune.BLOCK_FACING_6);

        int target = 100000;
        if (currentSide == Direction.UP) {
            progress += 1000; // can be free because of gravity
        }
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            pickupItems(currentSide);
        }

        putInNearInventories(currentSide);
    }

    @Override
//    public void readFromNBT(CompoundNBT nbt)
    public void load(BlockState state, CompoundNBT nbt) {
//        super.readFromNBT(nbt);
        super.load(state, nbt);
        progress = nbt.getInt("progress");
        battery.deserializeNBT(nbt.getCompound("battery"));
    }

    @Override
//    public CompoundNBT writeToNBT(CompoundNBT nbt)
    public CompoundNBT save(CompoundNBT nbt) {
//        super.writeToNBT(nbt);
        super.save(nbt);
        nbt.putInt("progress", progress);
        nbt.put("battery", battery.serializeNBT());
        return nbt;
    }

    // IDebuggable

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
//        left.add("battery = " + battery.getDebugString());
//        left.add("progress = " + progress);
        left.add(new StringTextComponent("battery = " + battery.getDebugString()));
        left.add(new StringTextComponent("progress = " + progress));
    }

    // INamedContainerProvider

    @Override
    public ITextComponent getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerChute(BCFactoryMenuTypes.CHUTE, id, player, this);
    }
}
