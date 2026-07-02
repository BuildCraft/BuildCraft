/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import ct.buildcraft.builders.tile.TileArchitectTable;
import ct.buildcraft.core.client.BuildCraftLaserManager;
import ct.buildcraft.lib.client.render.laser.LaserBoxRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

public class RenderArchitectTable implements BlockEntityRenderer<TileArchitectTable> {
   
	public RenderArchitectTable(BlockEntityRendererProvider.Context bpc) {
	}
	
	@Override
    public void render(TileArchitectTable tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light, int overlay) {
        if (!tile.markerBox) {
            return;
        }
		
//        Minecraft.getInstance().getProfiler().push("bc");
//        Minecraft.getInstance().getProfiler().push("architect_table");

        matrix.pushPose();
		VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
		Matrix4f pose = matrix.last().pose();
		Matrix3f normal = matrix.last().normal();
		BlockPos pos = tile.getBlockPos();
		matrix.translate(-pos.getX(), -pos.getY(), -pos.getZ());
//        Minecraft.getInstance().getProfiler().push("box");
        LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_READ, pose, normal, bb, true);
//        Minecraft.getInstance().getProfiler().pop();

        matrix.popPose();

//        Minecraft.getInstance().getProfiler().pop();
//        Minecraft.getInstance().getProfiler().pop();
    }

    
    @Override
    public boolean shouldRenderOffScreen(TileArchitectTable te) {
        return true;
    }
}
