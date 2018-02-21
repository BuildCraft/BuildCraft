/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.item.ItemDebugger;

public class ClientDebuggables {
    public static final List<String> SERVER_LEFT = new ArrayList<>();
    public static final List<String> SERVER_RIGHT = new ArrayList<>();

    @Nullable
    public static IDebuggable getDebuggableObject(RayTraceResult mouseOver) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.reducedDebugInfo ||
            mc.player.hasReducedDebug() ||
            !mc.gameSettings.showDebugInfo ||
            !ItemDebugger.isShowDebugInfo(mc.player)) {
            return null;
        }
        if (mouseOver == null) {
            return null;
        }
        RayTraceResult.Type type = mouseOver.typeOfHit;
        WorldClient world = mc.world;
        if (world == null) {
            return null;
        }
        if (type == RayTraceResult.Type.BLOCK) {
            BlockPos pos = mouseOver.getBlockPos();
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IDebuggable) {
                return (IDebuggable) tile;
            }
        } else if (type == RayTraceResult.Type.ENTITY) {
            Entity entity = mouseOver.entityHit;
            if (entity instanceof IDebuggable) {
                return (IDebuggable) entity;
            }
        }
        return null;
    }
}
