/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import java.util.Collections;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;

import buildcraft.builders.snapshot.ITileForSnapshotBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.core.client.BuildCraftLaserManager;

@SideOnly(Side.CLIENT)
public class RenderSnapshotBuilder {
    public static <T extends ITileForSnapshotBuilder> void render(
            SnapshotBuilder<T> snapshotBuilder,
            World world,
            BlockPos tilePos,
            double x,
            double y,
            double z,
            float partialTicks,
            BufferBuilder bb
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
                        x - tilePos.getX() + pos.x,
                        y - tilePos.getY() + pos.y,
                        z - tilePos.getZ() + pos.z,
                        item,
                        world.getCombinedLight(tilePos, 0),
                        EnumFacing.SOUTH,
                        bb
                );
            }
            ItemRenderUtil.endItemBatch();
        }

        Vec3d robotPos = snapshotBuilder.robotPos;
        if (robotPos != null) {
            if (snapshotBuilder.prevRobotPos != null) {
                robotPos = snapshotBuilder.prevRobotPos.add(robotPos.subtract(snapshotBuilder.prevRobotPos).scale(partialTicks));
            }

            RenderEntity.renderOffsetAABB(
                    new AxisAlignedBB(
                            robotPos.subtract(VecUtil.VEC_HALF),
                            robotPos.add(VecUtil.VEC_HALF)
                    ),
                    x - tilePos.getX(),
                    y - tilePos.getY(),
                    z - tilePos.getZ()
            );

            bb.setTranslation(x - tilePos.getX(), y - tilePos.getY(), z - tilePos.getZ());

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
                                robotPos,
                                new Vec3d(breakTask.pos).add(VecUtil.VEC_HALF),
                                1 / 16D
                        ),
                        bb
                );
            }
        }

        bb.setTranslation(0, 0, 0);
    }
}
