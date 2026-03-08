/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileFiller;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderFiller extends TileEntityRenderer<TileFiller> {
    public RenderFiller(TileEntityRendererDispatcher context) {
        super(context);
    }

    @Override
//    public void renderTileEntityFast(TileFiller tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder bb)
    public void render(TileFiller tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("filler");

        Minecraft.getInstance().getProfiler().push("main");
        IVertexBuilder bb = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());
        if (tile.getBuilder() != null) {
//            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getWorld(), tile.getPos(), x, y, z, partialTicks, bb);
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getLevel(), tile.getBlockPos(), partialTicks, poseStack, bb);
        }
        Minecraft.getInstance().getProfiler().pop();

        Minecraft.getInstance().getProfiler().push("box");
        if (tile.markerBox) {
//            bb.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
            poseStack.pushPose();
            poseStack.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());
            LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_WRITE, poseStack.last(), bb, true);
//            bb.setTranslation(0, 0, 0);
            poseStack.popPose();
        }
        Minecraft.getInstance().getProfiler().pop();

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
//    public boolean isGlobalRenderer(TileFiller te)
    public boolean shouldRenderOffScreen(TileFiller tile) {
        return true;
    }
}
