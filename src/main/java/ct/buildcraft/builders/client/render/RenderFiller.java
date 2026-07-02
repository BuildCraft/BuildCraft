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
        if (tile.getBuilder() != null) {
            matrix.pushPose();
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getLevel(), tile.getBlockPos(), partialTicks, matrix, buffer, itemRenderer);
            matrix.popPose();
        }

        if (tile.markerBox) {
            VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
            matrix.pushPose();
            matrix.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());
            Matrix4f boxPose = matrix.last().pose();
            Matrix3f boxNormal = matrix.last().normal();
            LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_WRITE, boxPose, boxNormal, bb, true);
            matrix.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileFiller te) {
        return true;
    }
}
