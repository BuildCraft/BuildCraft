/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.inventory.TransactorEntityItem;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.factory.block.BlockChute;

public class TileChute extends TileBC_Neptune implements ITickable, IDebuggable {
    private static final int PICKUP_RADIUS = 3;
    private static final int PICKUP_MAX = 3;
    public final ItemHandlerSimple inv = itemManager.addInvHandler(
        "inv",
        4,
        EnumAccess.INSERT,
        EnumPipePart.VALUES
    );
    @SuppressWarnings("PointlessArithmeticExpression")
    private final MjBattery battery = new MjBattery(1 * MjAPI.MJ);
    private int progress = 0;

    public TileChute() {
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    public static boolean hasInventoryAtPosition(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        return ItemTransactorHelper.getTransactor(tile, side.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    private void pickupItems(EnumFacing currentSide) {
        world.getEntitiesWithinAABB(
            EntityItem.class,
            new AxisAlignedBB(pos.offset(currentSide, PICKUP_RADIUS)).grow(PICKUP_RADIUS)
        ).stream()
            .limit(PICKUP_MAX)
            .map(TransactorEntityItem::new)
            .forEach(transactor -> {
                if (inv.insert(
                    transactor.extract(
                        StackFilter.ALL,
                        0,
                        Integer.MAX_VALUE,
                        true
                    ),
                    true,
                    true
                ).isEmpty()) {
                    inv.insert(
                        transactor.extract(
                            StackFilter.ALL,
                            0,
                            Integer.MAX_VALUE,
                            false
                        ),
                        true,
                        false
                    );
                }
            });
    }

    private void putInNearInventories(EnumFacing currentSide) {
        List<EnumFacing> sides = new ArrayList<>(Arrays.asList(EnumFacing.VALUES));
        Collections.shuffle(sides, new Random());
        sides.removeIf(Predicate.isEqual(currentSide));
        Stream.<Pair<EnumFacing, ICapabilityProvider>>concat(
            sides.stream()
                .map(side -> Pair.of(side, world.getTileEntity(pos.offset(side)))),
            sides.stream()
                .flatMap(side ->
                    world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.offset(side))).stream()
                        .map(entity -> Pair.of(side, entity))
                )
        )
            .map(sideProvider -> ItemTransactorHelper.getTransactor(sideProvider.getRight(), sideProvider.getLeft().getOpposite()))
            .filter(Predicate.isEqual(NoSpaceTransactor.INSTANCE).negate())
            .forEach(transactor -> {
                ItemStack item = inv.extract(
                    stack -> {
                        ItemStack leftOver = transactor.insert(stack.copy(), false, true);
                        return leftOver.isEmpty() || leftOver.getCount() < stack.getCount();
                    },
                    1,
                    1,
                    false
                );
                if (!item.isEmpty()) {
                    transactor.insert(item, false, false);
                }
            });
    }

    // ITickable

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        if (!(world.getBlockState(pos).getBlock() instanceof BlockChute)) {
            return;
        }

        battery.tick(getWorld(), getPos());

        EnumFacing currentSide = world.getBlockState(pos).getValue(BlockBCBase_Neptune.BLOCK_FACING_6);

        int target = 100000;
        if (currentSide == EnumFacing.UP) {
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
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getInteger("progress");
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", progress);
        nbt.setTag("battery", battery.serializeNBT());
        return nbt;
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("progress = " + progress);
    }
}
