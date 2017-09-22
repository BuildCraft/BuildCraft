/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;

public class ClientDebuggables {
    public static final List<String> SERVER_LEFT = new ArrayList<>();
    public static final List<String> SERVER_RIGHT = new ArrayList<>();

    @SideOnly(Side.CLIENT)
    private static <T extends TileEntity & IDebuggable> Optional<T> getDebuggableObject(RayTraceResult mouseOver) {
        RayTraceResult.Type type = mouseOver.typeOfHit;
        WorldClient world = Minecraft.getMinecraft().world;
        if (type == RayTraceResult.Type.BLOCK) {
            BlockPos pos = mouseOver.getBlockPos();
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IDebuggable) {
                // noinspection RedundantCast
                return Optional.of((T) (TileEntity & IDebuggable) tile);
            }
        }
        return Optional.empty();
    }


    @SideOnly(Side.CLIENT)
    public static <T extends TileEntity & IDebuggable> Optional<Pair<T, EnumFacing>> getDebuggableTileSide() {
        return Optional.ofNullable(Minecraft.getMinecraft().objectMouseOver)
            .flatMap(mouseOver ->
                ClientDebuggables.<T>getDebuggableObject(mouseOver)
                    .map(debuggableObject -> Pair.of(debuggableObject, mouseOver.sideHit))
            );
    }
}
