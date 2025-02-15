/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.client.render;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class RenderMarkerVolume implements BlockEntityRenderer<TileMarkerVolume> {
    private static final double SCALE = 1 / 16.2; // smaller than normal lasers

//    public static final RenderMarkerVolume INSTANCE = new RenderMarkerVolume();

    private static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
    private static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);

    public RenderMarkerVolume(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public boolean isGlobalRenderer(TileMarkerVolume te)
    public boolean shouldRenderOffScreen(TileMarkerVolume te) {
        return true;
    }

    @Override
    public int getViewDistance() {
        // Calen: as beacon and endGateway
        return BCCoreConfig.markerMaxDistance * 2;
    }

    @Override
//    public void render(TileMarkerVolume marker, double tileX, double tileY, double tileZ, float partialTicks, int destroyStage, float alpha)
    public void render(TileMarkerVolume marker, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // Calen: red laser rendered in VolumeConnection#renderInWorld, blue laser rendered here
        if (marker == null || !marker.isShowingSignals()) return;

        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("marker");
        Minecraft.getInstance().getProfiler().push("volume");

        poseStack.pushPose();

        // Calen: 1.18.2 should not call these
//        DetachedRenderer.fromWorldOriginPre(Minecraft.getMinecraft().player, partialTicks);
//        RenderHelper.disableStandardItemLighting();
//        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        VolumeConnection volume = marker.getCurrentConnection();
        Set<Axis> taken = volume == null ? ImmutableSet.of() : volume.getConnectedAxis();

        // 1.18.2 poseStack has translated to marker pos before #render called
        // LaserRenderer_BC8#renderLaserDynamic should accept world pos, not the offset pos
        Vec3 start = VecUtil.add(VEC_HALF, marker.getBlockPos());
        BlockPos markerPos = marker.getBlockPos();
        poseStack.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());

        for (Direction face : Direction.VALUES) {
            if (taken.contains(face.getAxis())) {
                continue;
            }
            Vec3 end = VecUtil.offset(start, face, BCCoreConfig.markerMaxDistance);
            renderLaser(start, end, face.getAxis(), poseStack);
        }

        poseStack.popPose();

//        RenderHelper.enableStandardItemLighting();
//        DetachedRenderer.fromWorldOriginPost();

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    private static void renderLaser(Vec3 minWorldPos, Vec3 maxWorldPos, Axis axis, PoseStack poseStack) {
        Direction faceForMin = VecUtil.getFacing(axis, true);
        Direction faceForMax = VecUtil.getFacing(axis, false);
        Vec3 one = offset(minWorldPos, faceForMin);
        Vec3 two = offset(maxWorldPos, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
//        LaserRenderer_BC8.renderLaserStatic(data);
        LaserRenderer_BC8.renderLaserStatic(data, poseStack.last());
    }

    private static Vec3 offset(Vec3 vec, Direction face) {
        double by = 1 / 16.0;
        if (face == Direction.DOWN) {
            return vec.add(0, -by, 0);
        } else if (face == Direction.UP) {
            return vec.add(0, by, 0);
        } else if (face == Direction.EAST) {
            return vec.add(by, 0, 0);
        } else if (face == Direction.WEST) {
            return vec.add(-by, 0, 0);
        } else if (face == Direction.SOUTH) {
            return vec.add(0, 0, by);
        } else {// North
            return vec.add(0, 0, -by);
        }
    }
}
