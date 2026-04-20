/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.lib.item.ItemDebugger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ClientDebuggables {
    public static final List<String> SERVER_LEFT = new ArrayList<>();
    public static final List<String> SERVER_RIGHT = new ArrayList<>();

    @Nullable
    public static IDebuggable getDebuggableObject(HitResult mouseOver) {
        if (mouseOver == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.reducedDebugInfo().get() ||
            mc.player.isReducedDebugInfo() ||
            !mc.options.renderDebug ||
            !ItemDebugger.isShowDebugInfo(mc.player)) {
            return null;
        }
        HitResult.Type type = mouseOver.getType();
        ClientLevel world = mc.level;
        if (world == null) {
            return null;
        }
        if (type == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)mouseOver).getBlockPos();
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IDebuggable) {
                return (IDebuggable) tile;
            }
        } else if (type == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult)mouseOver).getEntity();
            if (entity instanceof IDebuggable) {
                return (IDebuggable) entity;
            }
        }
        return null;
    }
}
