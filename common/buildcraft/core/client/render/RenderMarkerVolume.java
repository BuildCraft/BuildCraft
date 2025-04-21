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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class RenderMarkerVolume extends TileEntityRenderer<TileMarkerVolume> {
    private static final double SCALE = 1 / 16.2; // smaller than normal lasers

//    public static final RenderMarkerVolume INSTANCE = new RenderMarkerVolume();

    private static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
    private static final Vector3d VEC_HALF = new Vector3d(0.5, 0.5, 0.5);

    public RenderMarkerVolume(TileEntityRendererDispatcher context) {
        super(context);
    }

    @Override
//    public boolean isGlobalRenderer(TileMarkerVolume te)
    public boolean shouldRenderOffScreen(TileMarkerVolume te) {
        return true;
    }

    @Override
//    public void render(TileMarkerVolume marker, double tileX, double tileY, double tileZ, float partialTicks, int destroyStage, float alpha)
    public void render(TileMarkerVolume marker, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
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
        Vector3d start = VecUtil.add(VEC_HALF, marker.getBlockPos());
        BlockPos markerPos = marker.getBlockPos();
        poseStack.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());

        for (Direction face : Direction.values()) {
            if (taken.contains(face.getAxis())) {
                continue;
            }
            Vector3d end = VecUtil.offset(start, face, BCCoreConfig.markerMaxDistance);
            renderLaser(start, end, face.getAxis(), poseStack);
        }

        poseStack.popPose();

//        RenderHelper.enableStandardItemLighting();
//        DetachedRenderer.fromWorldOriginPost();

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    private static void renderLaser(Vector3d minWorldPos, Vector3d maxWorldPos, Axis axis, MatrixStack poseStack) {
        Direction faceForMin = VecUtil.getFacing(axis, true);
        Direction faceForMax = VecUtil.getFacing(axis, false);
        Vector3d one = offset(minWorldPos, faceForMin);
        Vector3d two = offset(maxWorldPos, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
//        LaserRenderer_BC8.renderLaserStatic(data);
        LaserRenderer_BC8.renderLaserStatic(data, poseStack.last());
    }

    private static Vector3d offset(Vector3d vec, Direction face) {
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
