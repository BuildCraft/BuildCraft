/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.client.render;

import java.util.Collections;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.builders.snapshot.ITileForSnapshotBuilder;
import ct.buildcraft.builders.snapshot.SnapshotBuilder;
import ct.buildcraft.core.client.BuildCraftLaserManager;
import ct.buildcraft.lib.client.model.ModelUtil;
import ct.buildcraft.lib.client.render.ItemRenderUtil;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8;
import ct.buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import ct.buildcraft.lib.misc.MathUtil;
import ct.buildcraft.lib.misc.VecUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSnapshotBuilder {
    public static <T extends ITileForSnapshotBuilder> void render(
            SnapshotBuilder<T> snapshotBuilder,
            Level world,
            BlockPos tilePos,
            float partialTicks,
            PoseStack matrix,
            MultiBufferSource buffer,
            ItemRenderer itemRenderer
    ) {
    	Matrix4f pose = matrix.last().pose();
		Matrix3f normal = matrix.last().normal();
		 matrix.translate( -tilePos.getX(), - tilePos.getY(), -tilePos.getZ());
        for (SnapshotBuilder<T>.PlaceTask placeTask : snapshotBuilder.clientPlaceTasks) {
            Vec3 prevPos = snapshotBuilder.prevClientPlaceTasks.stream()
                .filter(renderTaskLocal -> renderTaskLocal.pos.equals(placeTask.pos))
                .map(snapshotBuilder::getPlaceTaskItemPos)
                .findFirst()
                .orElse(snapshotBuilder.getPlaceTaskItemPos(snapshotBuilder.new PlaceTask(tilePos, Collections.emptyList(), 0L)));
            Vec3 pos = prevPos.add(snapshotBuilder.getPlaceTaskItemPos(placeTask).subtract(prevPos).scale(partialTicks));
            matrix.translate(pos.x, pos.y, pos.z);
            int i = 0;
            for (ItemStack item : placeTask.items) {
            	itemRenderer.renderStatic(item, TransformType.GROUND,
            			15728640, OverlayTexture.NO_OVERLAY,
            			matrix, buffer, i++);
/*                ItemRenderUtil.renderItemStack(
                     - tilePos.getX() + pos.x,
                     - tilePos.getY() + pos.y,
                     - tilePos.getZ() + pos.z,
                    item,
                    world.getRawBrightness(new BlockPos(pos), 0),
                    Direction.SOUTH,
                    bb
                );*/
            	
            }
            matrix.translate(-pos.x, -pos.y, -pos.z);
  //          ItemRenderUtil.endItemBatch();
        }
        VertexConsumer bb = buffer.getBuffer(RenderType.cutoutMipped());
        Vec3 robotPos = snapshotBuilder.robotPos;
       
        if (robotPos != null) {
            if (snapshotBuilder.prevRobotPos != null) {
                robotPos = snapshotBuilder.prevRobotPos.add(robotPos.subtract(snapshotBuilder.prevRobotPos).scale(partialTicks));
            }
            BlockPos robp = new BlockPos(robotPos);

            int i = 0;
            for (Direction face : Direction.values()) {
//            	RenderSystem._setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                ModelUtil.createFace(
                    face,
                    new Vector3f(robotPos),
                    new Vector3f(4 / 16F, 4 / 16F, 4 / 16F),
                    new ModelUtil.UvFaceData(
                        BCBuildersSprites.ROBOT.getInterpU((i * 8) / 64D),
                        BCBuildersSprites.ROBOT.getInterpV(0 / 64D),
                        BCBuildersSprites.ROBOT.getInterpU(((i + 1) * 8) / 64D),
                        BCBuildersSprites.ROBOT.getInterpV(8 / 64D)
                    )
                )
                    .lighti(world.getBrightness(LightLayer.BLOCK, robp), world.getBrightness(LightLayer.SKY, robp))
                    .render(pose, normal, bb);
                i++;
            }
            
            matrix.translate(robotPos.x(), robotPos.y() , robotPos.z() );
            //matrix.translate(0, 0, 0);
            for (SnapshotBuilder.BreakTask breakTask : snapshotBuilder.clientBreakTasks) {
                LaserRenderer_BC8.renderLaserDynamic(
                    pose, normal,
                    new LaserData_BC8(
                        BuildCraftLaserManager.POWERS[(int) Math.round(
                            MathUtil.clamp(
                                breakTask.power * 1D / breakTask.getTarget(),
                                0D,
                                1D
                            ) * (BuildCraftLaserManager.POWERS.length - 1)
                        )],
                        robotPos.subtract(new Vec3(0, 0.27, 0)),
                        Vec3.atLowerCornerOf(breakTask.pos).add(VecUtil.VEC_HALF),
                        1 / 16D
                    ),
                	bb
                );
            }
        }

    }
}
