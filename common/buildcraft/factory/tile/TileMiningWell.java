/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;

import buildcraft.lib.inventory.AutomaticProvidingTransactor;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReciver;

import buildcraft.factory.BCFactoryBlocks;

public class TileMiningWell extends TileMiner {
    public TileMiningWell() {
        super();
        caps.addCapabilityInstance(CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE, EnumPipePart.VALUES);
    }

    @Override
    protected void mine() {
        if (currentPos != null && canBreak()) {
            long target = BlockUtil.computeBlockBreakPower(world, currentPos);
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
                EntityPlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) world, getOwner(), pos);
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, currentPos, world.getBlockState(currentPos), fakePlayer);
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (!breakEvent.isCanceled()) {
                    NonNullList<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) world, currentPos, getOwner());
                    if (stacks != null) {
                        for (ItemStack stack : stacks) {
                            InventoryUtil.addToBestAcceptor(world, pos, null, stack);
                        }
                    }
                    world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                    world.destroyBlock(currentPos, false);
                }
                nextPos();
                updateLength();
            } else {
                if (!world.isAirBlock(currentPos)) {
                    world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                }
            }
        } else {
            nextPos();
            updateLength();
        }
    }

    private boolean canBreak() {
        return !world.isAirBlock(currentPos) && !BlockUtil.isUnbreakableBlock(world, currentPos, getOwner());
    }

    private void nextPos() {
        for (currentPos = pos.down(); currentPos.getY() >= 0; currentPos = currentPos.down()) {
            if (canBreak()) {
                updateLength();
                return;
            } else if (!world.isAirBlock(currentPos) && world.getBlockState(currentPos).getBlock() != BCFactoryBlocks.tube) {
                break;
            }
        }
        updateLength();
        currentPos = null;
    }

    @Override
    protected void initCurrentPos() {
        if (currentPos == null) {
            nextPos();
        }
    }

    @Override
    public void invalidate() {
        if (currentPos != null) {
            world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
        }
        super.invalidate();
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReciver(battery);
    }
}
