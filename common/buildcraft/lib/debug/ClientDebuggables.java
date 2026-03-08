/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.item.ItemDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClientDebuggables {
    // public static final List<String> SERVER_LEFT = new ArrayList<>();
    public static final List<ITextComponent> SERVER_LEFT = new ArrayList<>();
    // public static final List<String> SERVER_RIGHT = new ArrayList<>();
    public static final List<ITextComponent> SERVER_RIGHT = new ArrayList<>();

    @Nullable
    public static IDebuggable getDebuggableObject(RayTraceResult mouseOver) {
        Minecraft mc = Minecraft.getInstance();
        if (
//                mc.gameSettings.reducedDebugInfo ||
                mc.options.reducedDebugInfo ||
//                        mc.player.hasReducedDebug() ||
                        mc.player.isReducedDebugInfo() ||
//                        !mc.gameSettings.showDebugInfo ||
                        !mc.options.renderDebug ||
                        !ItemDebugger.isShowDebugInfo(mc.player)
        )
        {
            return null;
        }
        if (mouseOver == null) {
            return null;
        }
//        RayTraceResult.Type type = mouseOver.typeOfHit;
        Type type = mouseOver.getType();
        ClientWorld world = mc.level;
        if (world == null) {
            return null;
        }
        if (type == Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) mouseOver).getBlockPos();
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IDebuggable) {
                return (IDebuggable) tile;
            }
        }
//        else if (type == RayTraceResult.Type.ENTITY)
        else if (type == Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) mouseOver).getEntity();
            if (entity instanceof IDebuggable) {
                return (IDebuggable) entity;
            }
        }
        return null;
    }
}
