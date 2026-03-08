/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import buildcraft.core.marker.volume.IFastAddonRenderer;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.ModelLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AddonRendererFillerPlanner implements IFastAddonRenderer<AddonFillerPlanner> {
    @Override
//    public void renderAddonFast(AddonFillerPlanner addon, PlayerEntity player, float partialTicks, BufferBuilder vb)
    public void renderAddonFast(AddonFillerPlanner addon, PlayerEntity player, Entry pose, float partialTicks, IVertexBuilder vb) {
        if (addon.buildingInfo == null) {
            return;
        }
        Minecraft.getInstance().getProfiler().push("filler_planner");

        Minecraft.getInstance().getProfiler().push("iter");
        List<BlockPos> list = StreamSupport.stream(
//                        BlockPos.getAllInBoxMutable(addon.buildingInfo.box.min(), addon.buildingInfo.box.max()).spliterator(),
                        BlockPos.betweenClosed(addon.buildingInfo.box.min(), addon.buildingInfo.box.max()).spliterator(),
                        false
                )
                .filter(blockPos ->
                        addon.buildingInfo.getSnapshot().data.get(
                                addon.buildingInfo.getSnapshot().posToIndex(
                                        addon.buildingInfo.fromWorld(blockPos)
                                )
                        )
                )
//                .filter(player.level::isAirBlock)
                .filter(player.level::isEmptyBlock)
//                .map(BlockPos.Mutable::toImmutable)
//                .map(BlockPos.Mutable::immutable) // Calen: betweenClosed ret mutable BlockPos
                .collect(Collectors.toCollection(ArrayList::new));
        Minecraft.getInstance().getProfiler().pop();

        Minecraft.getInstance().getProfiler().push("sort");
//        list.sort(Comparator.<BlockPos>comparingDouble(p -> player.getPositionVector().squareDistanceTo(new Vector3d(p))).reversed());
        list.sort(Comparator.<BlockPos>comparingDouble(p -> player.position().distanceToSqr(Vector3d.atLowerCornerOf(p))).reversed());
        Minecraft.getInstance().getProfiler().pop();

        Minecraft.getInstance().getProfiler().push("render");
        Matrix4f posePose = pose.pose();
        Matrix3f normal = pose.normal();
        for (BlockPos p : list) {
            AxisAlignedBB bb = new AxisAlignedBB(p, p.offset(1, 1, 1)).inflate(-0.1);
//            TextureAtlasSprite s = ModelLoader.White.INSTANCE;
            TextureAtlasSprite s = ModelLoader.White.instance();

            vb.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.minZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.minZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.minZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.minZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

            vb.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.maxZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.maxZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

            vb.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.minZ).color(127, 127, 127, 127).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.minZ).color(127, 127, 127, 127).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ).color(127, 127, 127, 127).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.maxZ).color(127, 127, 127, 127).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

            vb.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.maxZ).color(255, 255, 255, 127).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ).color(255, 255, 255, 127).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.minZ).color(255, 255, 255, 127).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.minZ).color(255, 255, 255, 127).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

            vb.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.maxZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.maxZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.minX, (float) bb.maxY, (float) bb.minZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.minX, (float) bb.minY, (float) bb.minZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();

            vb.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.minZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.minZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
            vb.vertex(posePose, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 0).normal(normal, 1, 1, 1).endVertex();
        }
        Minecraft.getInstance().getProfiler().pop();

        Minecraft.getInstance().getProfiler().pop();
    }
}
