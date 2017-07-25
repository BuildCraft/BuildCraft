/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import java.util.Collections;

import javax.vecmath.Point3f;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;

import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.ITileForSnapshotBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderSnapshotBuilder {
    public static <T extends ITileForSnapshotBuilder> void render(
        SnapshotBuilder<T> snapshotBuilder,
        World world,
        BlockPos tilePos,
        double x,
        double y,
        double z,
        float partialTicks,
        VertexBuffer vb
    ) {
        for (SnapshotBuilder<T>.PlaceTask placeTask : snapshotBuilder.clientPlaceTasks) {
            Vec3d prevPos = snapshotBuilder.prevClientPlaceTasks.stream()
                .filter(renderTaskLocal -> renderTaskLocal.pos.equals(placeTask.pos))
                .map(snapshotBuilder::getPlaceTaskItemPos)
                .findFirst()
                .orElse(snapshotBuilder.getPlaceTaskItemPos(snapshotBuilder.new PlaceTask(tilePos, Collections.emptyList(), 0L)));
            Vec3d pos = prevPos.add(snapshotBuilder.getPlaceTaskItemPos(placeTask).subtract(prevPos).scale(partialTicks));
            for (ItemStack item : placeTask.items) {
                ItemRenderUtil.renderItemStack(
                    x - tilePos.getX() + pos.xCoord,
                    y - tilePos.getY() + pos.yCoord,
                    z - tilePos.getZ() + pos.zCoord,
                    item,
                    world.getCombinedLight(new BlockPos(pos), 0),
                    EnumFacing.SOUTH,
                    vb
                );
            }
            ItemRenderUtil.endItemBatch();
        }

        Vec3d robotPos = snapshotBuilder.robotPos;
        if (robotPos != null) {
            if (snapshotBuilder.prevRobotPos != null) {
                robotPos = snapshotBuilder.prevRobotPos.add(robotPos.subtract(snapshotBuilder.prevRobotPos).scale(partialTicks));
            }

            vb.setTranslation(x - tilePos.getX(), y - tilePos.getY(), z - tilePos.getZ());

            int i = 0;
            for (EnumFacing face : EnumFacing.VALUES) {
                ModelUtil.createFace(
                    face,
                    new Point3f((float) robotPos.xCoord, (float) robotPos.yCoord, (float) robotPos.zCoord),
                    new Point3f(4 / 16F, 4 / 16F, 4 / 16F),
                    new ModelUtil.UvFaceData(
                        BCBuildersSprites.ROBOT.getInterpU((i * 8) / 64D),
                        BCBuildersSprites.ROBOT.getInterpV(0 / 64D),
                        BCBuildersSprites.ROBOT.getInterpU(((i + 1) * 8) / 64D),
                        BCBuildersSprites.ROBOT.getInterpV(8 / 64D)
                    )
                )
                    .lighti(world.getCombinedLight(new BlockPos(robotPos), 0))
                    .render(vb);
                i++;
            }

            for (SnapshotBuilder.BreakTask breakTask : snapshotBuilder.clientBreakTasks) {
                LaserRenderer_BC8.renderLaserDynamic(
                    new LaserData_BC8(
                        BuildCraftLaserManager.POWERS[(int) Math.round(
                            MathUtil.clamp(
                                breakTask.power * 1D / breakTask.getTarget(),
                                0D,
                                1D
                            ) * (BuildCraftLaserManager.POWERS.length - 1)
                        )],
                        robotPos.subtract(new Vec3d(0, 0.47, 0)),
                        new Vec3d(breakTask.pos).add(VecUtil.VEC_HALF),
                        1 / 16D
                    ),
                    vb
                );
            }
        }

        vb.setTranslation(0, 0, 0);
    }
}
