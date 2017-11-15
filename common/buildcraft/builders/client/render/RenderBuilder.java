/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.render.laser.RenderLaserBox;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.builders.tile.TileBuilder;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderBuilder extends FastTESR<TileBuilder> {
    private static final double OFFSET = 0.1;

    @Override
    public void renderTileEntityFast(@Nonnull TileBuilder tile, double x, double y, double z, float partialTicks, int destroyStage, @Nonnull VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("builder");

        vb.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        Minecraft.getMinecraft().mcProfiler.startSection("box");
        Box box = tile.getBox();
        RenderLaserBox.renderDynamic(box, BuildCraftLaserManager.STRIPES_WRITE, vb, false);

        Minecraft.getMinecraft().mcProfiler.endStartSection("path");

        List<BlockPos> path = tile.path;
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
                    Vec3d from = new Vec3d(last).add(VecUtil.VEC_HALF);
                    Vec3d to = new Vec3d(p).add(VecUtil.VEC_HALF);
                    Vec3d one = offset(from, to);
                    Vec3d two = offset(to, from);
                    LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION, one, two, 1 / 16.1);
                    LaserRenderer_BC8.renderLaserDynamic(data, vb);
                }
                last = p;
            }
        }

        Minecraft.getMinecraft().mcProfiler.endSection();

        vb.setTranslation(0, 0, 0);

        if (tile.getBuilder() != null) {
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getWorld(), tile.getPos(), x, y, z, partialTicks, vb);
        }

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static Vec3d offset(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, OFFSET));
    }

    @Override
    public boolean isGlobalRenderer(TileBuilder te) {
        return true;
    }
}
