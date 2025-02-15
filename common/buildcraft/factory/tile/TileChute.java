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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

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

    public TileChute(BlockPos pos, BlockState blockState) {
        super(BCFactoryBlocks.chuteTile.get(), pos, blockState);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    // public static boolean hasInventoryAtPosition(IBlockAccess world, BlockPos pos, Direction side)
    public static boolean hasInventoryAtPosition(LevelAccessor world, BlockPos pos, Direction side) {
        BlockEntity tile = world.getBlockEntity(pos);
        return ItemTransactorHelper.getTransactor(tile, side.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    private void pickupItems(Direction currentSide) {
        AABB aabb = BoundingBoxUtil.extrudeFace(getBlockPos(), currentSide, 0.25);
        int count = PICKUP_MAX;
//        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, aabb, EntitySelectors.IS_ALIVE))
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, aabb, EntitySelector.ENTITY_STILL_ALIVE)) {
            int moved = ItemTransactorHelper.move(new TransactorEntityItem(entity), inv, count);
            count -= moved;
            if (count <= 0) {
                return;
            }
        }
    }

    private void putInNearInventories(Direction currentSide) {
        boolean[] didWork = { false };
        List<Direction> sides = new ArrayList<>(Arrays.asList(Direction.VALUES));
        Collections.shuffle(sides, new Random());
        sides.removeIf(Predicate.isEqual(currentSide));
        Stream.<Pair<Direction, ICapabilityProvider>>concat(
                        sides.stream()
                                .map(side -> Pair.of(side, level.getBlockEntity(worldPosition.relative(side)))),
                        sides.stream()
                                .flatMap(side ->
                                        level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.relative(side))).stream()
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
//    public void readFromNBT(CompoundTag nbt)
    public void load(CompoundTag nbt) {
//        super.readFromNBT(nbt);
        super.load(nbt);
        progress = nbt.getInt("progress");
        battery.deserializeNBT(nbt.getCompound("battery"));
    }

    @Override
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public void saveAdditional(CompoundTag nbt) {
//        super.writeToNBT(nbt);
        super.saveAdditional(nbt);
        nbt.putInt("progress", progress);
        nbt.put("battery", battery.serializeNBT());
//        return nbt;
    }

    // IDebuggable

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("battery = " + battery.getDebugString());
//        left.add("progress = " + progress);
        left.add(Component.literal("battery = " + battery.getDebugString()));
        left.add(Component.literal("progress = " + progress));
    }

    // MenuProvider

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerChute(BCFactoryMenuTypes.CHUTE, id, player, this);
    }
}
