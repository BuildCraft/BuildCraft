/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.item.ItemDebugger;

public class ClientDebuggables {
    public static final List<String> SERVER_LEFT = new ArrayList<>();
    public static final List<String> SERVER_RIGHT = new ArrayList<>();

    @Nullable
    public static IDebuggable getDebuggableObject(HitResult mouseOver) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.reducedDebugInfo().getValue() ||
            !mc.options.debugEnabled ||
            !ItemDebugger.isShowDebugInfo(mc.player)) {
            return null;
        }
        if (mouseOver == null) {
            return null;
        }
        ClientWorld world = mc.world;
        if (world == null) {
            return null;
        }
        if (mouseOver instanceof BlockHitResult) {
            BlockPos pos = ((BlockHitResult) mouseOver).getBlockPos();
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IDebuggable) {
                return (IDebuggable) tile;
            }
        } else if (mouseOver instanceof EntityHitResult) {
            Entity entity = ((EntityHitResult) mouseOver).getEntity();
            if (entity instanceof IDebuggable) {
                return (IDebuggable) entity;
            }
        }
        return null;
    }
}
