/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.client.render;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import ct.buildcraft.core.BCCoreConfig;
import ct.buildcraft.core.blockEntity.TileMarkerVolume;
import ct.buildcraft.core.client.BuildCraftLaserManager;
import ct.buildcraft.core.marker.VolumeConnection;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import ct.buildcraft.lib.misc.VecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

public class RenderMarkerVolume implements BlockEntityRenderer<TileMarkerVolume> {
    private static final double SCALE = 1 / 16.2; // smaller than normal lasers

    private static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
    private static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);

    private static final Minecraft mc = Minecraft.getInstance();
    
	public RenderMarkerVolume(BlockEntityRendererProvider.Context bpc) {
	}
    
	@Override
	public boolean shouldRenderOffScreen(TileMarkerVolume p_112306_) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public boolean shouldRender(TileMarkerVolume marker, Vec3 cameraPos) {
		return true;
	}
    
    @Override
	public void render(TileMarkerVolume marker, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int light, int overlay) {
    	 if (marker == null || !marker.isShowingSignals()) return;
    	 matrix.pushPose();
    	 matrix.translate(0.5f, 0.5f, 0.5f);
    	 Matrix4f pose = matrix.last().pose();
    	 Matrix3f normal = matrix.last().normal();
    	 VertexConsumer bb = buffer.getBuffer(RenderType.solid());

         mc.getProfiler().push("bc");
         mc.getProfiler().push("marker");
         mc.getProfiler().push("volume");

//         DetachedRenderer.fromWorldOriginPre(matrix, null, mc.player, partialTicks);
//         RenderHelper.disableStandardItemLighting();
         RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

         VolumeConnection volume = marker.getCurrentConnection();
         Set<Axis> taken = volume == null ? ImmutableSet.of() : volume.getConnectedAxis();

         Vec3 start = VecUtil.add(VEC_HALF.add(VEC_HALF), BlockPos.ZERO);
         for (Direction face : Direction.values()) {
             if (taken.contains(face.getAxis())) {
                 continue;
             }
             Vec3 end = VecUtil.offset(start, face, BCCoreConfig.markerMaxDistance);
             renderLaser(start, end, face.getAxis(), bb, pose, normal);
         }

//         RenderHelper.enableStandardItemLighting();
//         DetachedRenderer.fromWorldOriginPost(matrix, null);

         mc.getProfiler().pop();
         mc.getProfiler().pop();
         mc.getProfiler().pop();
         matrix.popPose();
		
	}



    private static void renderLaser(Vec3 min, Vec3 max, Axis axis, VertexConsumer bb, Matrix4f pose, Matrix3f normal) {
        Direction faceForMin = VecUtil.getFacing(axis, true);
        Direction faceForMax = VecUtil.getFacing(axis, false);
        Vec3 one = offset(min, faceForMin);
        Vec3 two = offset(max, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
        LaserRenderer_BC8.renderLaserDynamic(pose, normal, data, bb);
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
