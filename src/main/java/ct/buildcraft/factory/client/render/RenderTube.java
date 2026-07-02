/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.client.render;

import ct.buildcraft.factory.tile.TileMiner;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RenderTube implements BlockEntityRenderer<TileMiner> {
    private final LaserType laserType;

    public RenderTube(LaserType laserType) {
        this.laserType = laserType;
    }

    @Override
	public void render(TileMiner tile, float partialTicks, PoseStack matrix, MultiBufferSource builder,
			int light, int overlay) {
        if (tile.isComplete()) {
            return;
        }
        matrix.pushPose();
        VertexConsumer buffer = builder.getBuffer(RenderType.solid());
        matrix.translate(0.5f, 0.49f, 0.5f);
        Pose p = matrix.last();
        Matrix4f pose = p.pose();
        Matrix3f normal = p.normal();

        BlockPos pos = tile.getBlockPos();
        Vec3 start = new Vec3(0.5+pos.getX(), 0+pos.getY(), 0.5+pos.getZ());
        Vec3 end = new Vec3(0.5+pos.getX(), pos.getY()-tile.getLength(partialTicks), 0.5+pos.getZ());
        LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
        LaserRenderer_BC8.renderLaserDynamic(pose, normal, data, buffer);

        matrix.popPose();
	}
}

