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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class RenderTube extends TileEntityRenderer<TileMiner> {
    private final LaserType laserType;

    // public RenderTube(LaserType laserType)
    public RenderTube(TileEntityRendererDispatcher context, LaserType laserType) {
        super(context);
        this.laserType = laserType;
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileMiner tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileMiner tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
        if (tile.isComplete()) {
            return;
        }

        IVertexBuilder buffer = bufferSource.getBuffer(Atlases.solidBlockSheet());

//        double tubeY = tile.getPos().getY() - tile.getLength(partialTicks);
        double tubeY = tile.getBlockPos().getY() - tile.getLength(partialTicks);

//        BlockPos from = tile.getPos();
        BlockPos from = tile.getBlockPos();
//        buffer.setTranslation(x - from.getX(), y - from.getY(), z - from.getZ());
        poseStack.pushPose();
        poseStack.translate(-from.getX(), -from.getY(), -from.getZ());

        Vector3d start = new Vector3d(from.getX() + 0.5, from.getY(), from.getZ() + 0.5);

        Vector3d end = new Vector3d(from.getX() + 0.5, tubeY, from.getZ() + 0.5);

        LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
        LaserRenderer_BC8.renderLaserDynamic(data, poseStack.last(), buffer);

//        buffer.setTranslation(0, 0, 0);
        poseStack.popPose();
    }
}
