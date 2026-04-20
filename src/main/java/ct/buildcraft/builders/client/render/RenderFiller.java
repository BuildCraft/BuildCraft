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

import ct.buildcraft.builders.tile.TileFiller;
import ct.buildcraft.core.client.BuildCraftLaserManager;
import ct.buildcraft.lib.client.render.laser.LaserBoxRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderFiller implements BlockEntityRenderer<TileFiller> {
	
	protected final ItemRenderer itemRenderer; 
	
	public RenderFiller(BlockEntityRendererProvider.Context bpc) {
		itemRenderer = bpc.getItemRenderer();
	}

    @Override
    public void render(TileFiller tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light, int overlay) {
//        Minecraft.getInstance().getProfiler().push("bc");
//        Minecraft.getInstance().getProfiler().push("filler");
    	matrix.pushPose();
    	VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
       	Matrix4f pose = matrix.last().pose();
    	Matrix3f normal = matrix.last().normal();
//        Minecraft.getInstance().getProfiler().push("main");
        if (tile.getBuilder() != null) {
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getLevel(), tile.getBlockPos(), partialTicks, matrix, buffer, itemRenderer);
        }
//        Minecraft.getInstance().getProfiler().pop();

//        Minecraft.getInstance().getProfiler().push("box");
        if (tile.markerBox) {
 //           bb.setTranslation(x - tile.getBlockPos().getX(), y - tile.getBlockPos().getY(), z - tile.getBlockPos().getZ());
            LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_WRITE, pose, normal, bb, true);
 //           bb.setTranslation(0, 0, 0);
        }
        matrix.popPose();
//        Minecraft.getInstance().getProfiler().pop();

//        Minecraft.getInstance().getProfiler().pop();
//        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
    public boolean shouldRenderOffScreen(TileFiller te) {
        return true;
    }
}
