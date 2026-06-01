/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.builders.tile.TileBuilder;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderBuilder extends FastTESR<TileBuilder> {
    private static final double OFFSET = 0.1;

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void renderTileEntityFast(@Nonnull TileBuilder tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        MinecraftClient.getInstance().getProfiler().push("bc");
        MinecraftClient.getInstance().getProfiler().push("builder");

        // TODO(R.Chen): setTranslation removed — use MatrixStack instead: buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        MinecraftClient.getInstance().getProfiler().push("box");
        Box box = tile.getBox();
        LaserBoxRenderer.renderLaserBoxDynamic(box, BuildCraftLaserManager.STRIPES_WRITE, buffer, true);

        MinecraftClient.getInstance().getProfiler().swap("path");

        List<BlockPos> path = tile.path;
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
                    Vec3d from = new Vec3d(last.getX(), last.getY(), last.getZ()).add(VecUtil.VEC_HALF);
                    Vec3d to = new Vec3d(p.getX(), p.getY(), p.getZ()).add(VecUtil.VEC_HALF);
                    Vec3d one = offset(from, to);
                    Vec3d two = offset(to, from);
                    LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION, one, two, 1 / 16.1);
                    LaserRenderer_BC8.renderLaserDynamic(data, buffer);
                }
                last = p;
            }
        }

        MinecraftClient.getInstance().getProfiler().pop();

        // TODO(R.Chen): setTranslation removed — use MatrixStack instead: buffer.setTranslation(0, 0, 0);

        if (tile.getBuilder() != null) {
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getWorld(), tile.getPos(), x, y, z, partialTicks, buffer);
        }

        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    private static Vec3d offset(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, OFFSET));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isGlobalRenderer(TileBuilder te) {
        return true;
    }
}
