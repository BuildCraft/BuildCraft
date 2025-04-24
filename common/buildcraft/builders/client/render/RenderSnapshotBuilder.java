/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.ITileForSnapshotBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.VecUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;

@OnlyIn(Dist.CLIENT)
public class RenderSnapshotBuilder {
    //    public static <T extends ITileForSnapshotBuilder> void render(
//            SnapshotBuilder<T> snapshotBuilder,
//            World world,
//            BlockPos tilePos,
//            double x,
//            double y,
//            double z,
//            float partialTicks,
//            BufferBuilder bb
//    )
    public static <T extends ITileForSnapshotBuilder> void render(
            SnapshotBuilder<T> snapshotBuilder,
            World world,
            BlockPos tilePos,
            float partialTicks,
            MatrixStack poseStack,
            IVertexBuilder bb
    ) {
        for (SnapshotBuilder<T>.PlaceTask placeTask : snapshotBuilder.clientPlaceTasks) {
            Vector3d prevPos = snapshotBuilder.prevClientPlaceTasks.stream()
                    .filter(renderTaskLocal -> renderTaskLocal.pos.equals(placeTask.pos))
                    .map(snapshotBuilder::getPlaceTaskItemPos)
                    .findFirst()
                    .orElse(snapshotBuilder.getPlaceTaskItemPos(snapshotBuilder.new PlaceTask(tilePos, Collections.emptyList(), 0L)));
            Vector3d pos = prevPos.add(snapshotBuilder.getPlaceTaskItemPos(placeTask).subtract(prevPos).scale(partialTicks));
            poseStack.pushPose();
            poseStack.translate(
                    -tilePos.getX() + pos.x,
                    -tilePos.getY() + pos.y,
                    -tilePos.getZ() + pos.z
            );
            for (ItemStack item : placeTask.items) {
//                ItemRenderUtil.renderItemStack(
//                        x - tilePos.getX() + pos.x,
//                        y - tilePos.getY() + pos.y,
//                        z - tilePos.getZ() + pos.z,
//                        item,
//                        world.getCombinedLight(new BlockPos(pos), 0),
//                        Direction.SOUTH,
//                        bb
//                );
                ItemRenderUtil.renderItemStack(
                        item,
                        RenderUtil.getCombinedLight(world, new BlockPos(pos)),
                        Direction.SOUTH,
                        poseStack,
                        bb
                );
            }
            ItemRenderUtil.endItemBatch(poseStack);
            poseStack.popPose();
        }

        poseStack.pushPose();
        Vector3d robotPos = snapshotBuilder.robotPos;
        if (robotPos != null) {
            if (snapshotBuilder.prevRobotPos != null) {
                robotPos = snapshotBuilder.prevRobotPos.add(robotPos.subtract(snapshotBuilder.prevRobotPos).scale(partialTicks));
            }

//            bb.setTranslation(x - tilePos.getX(), y - tilePos.getY(), z - tilePos.getZ());
            poseStack.translate(-tilePos.getX(), -tilePos.getY(), -tilePos.getZ());

            int i = 0;
            for (Direction face : Direction.values()) {
                ModelUtil.createFace(
                                face,
                                new Vector3f((float) robotPos.x, (float) robotPos.y, (float) robotPos.z),
                                new Vector3f(4 / 16F, 4 / 16F, 4 / 16F),
                                new ModelUtil.UvFaceData(
                                        BCBuildersSprites.ROBOT.getInterpU((i * 8) / 64D),
                                        BCBuildersSprites.ROBOT.getInterpV(0 / 64D),
                                        BCBuildersSprites.ROBOT.getInterpU(((i + 1) * 8) / 64D),
                                        BCBuildersSprites.ROBOT.getInterpV(8 / 64D)
                                )
                        )
//                        .lighti(world.getCombinedLight(new BlockPos(robotPos), 0))
                        .lighti(RenderUtil.getCombinedLight(world, new BlockPos(robotPos)))
                        .render(poseStack.last(), bb);
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
                                robotPos.subtract(new Vector3d(0, 0.27, 0)),
                                Vector3d.atLowerCornerOf(breakTask.pos).add(VecUtil.VEC_HALF),
                                1 / 16D
                        ),
                        poseStack.last(),
                        bb
                );
            }
        }

//        bb.setTranslation(0, 0, 0);
        poseStack.popPose();
    }
}
