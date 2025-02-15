/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileMiner;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RenderTube implements BlockEntityRenderer<TileMiner> {
    private final LaserType laserType;

    // public RenderTube(LaserType laserType)
    public RenderTube(BlockEntityRendererProvider.Context context, LaserType laserType) {
        this.laserType = laserType;
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileMiner tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileMiner tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (tile.isComplete()) {
            return;
        }

        VertexConsumer buffer = bufferSource.getBuffer(Sheets.solidBlockSheet());

//        double tubeY = tile.getPos().getY() - tile.getLength(partialTicks);
        double tubeY = tile.getBlockPos().getY() - tile.getLength(partialTicks);

//        BlockPos from = tile.getPos();
        BlockPos from = tile.getBlockPos();
//        buffer.setTranslation(x - from.getX(), y - from.getY(), z - from.getZ());
        poseStack.pushPose();
        poseStack.translate(-from.getX(), -from.getY(), -from.getZ());

        Vec3 start = new Vec3(from.getX() + 0.5, from.getY(), from.getZ() + 0.5);

        Vec3 end = new Vec3(from.getX() + 0.5, tubeY, from.getZ() + 0.5);

        LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
        LaserRenderer_BC8.renderLaserDynamic(data, poseStack.last(), buffer);

//        buffer.setTranslation(0, 0, 0);
        poseStack.popPose();
    }
}
