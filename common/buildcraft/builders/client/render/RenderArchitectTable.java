/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderArchitectTable implements BlockEntityRenderer<TileArchitectTable> {
    public RenderArchitectTable(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void render(TileArchitectTable tile, double x, double y, double z, float partialTicks, int destroyStage, float partial)
    public void render(TileArchitectTable tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (!tile.markerBox) {
            return;
        }
        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("architect_table");

//        GL11.glPushMatrix();
        poseStack.pushPose();
//        GL11.glTranslated(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
        poseStack.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());
//        RenderHelper.disableStandardItemLighting();

        Minecraft.getInstance().getProfiler().push("box");
//        LaserBoxRenderer.renderLaserBoxStatic(tile.box, BuildCraftLaserManager.STRIPES_READ, true);
        LaserBoxRenderer.renderLaserBoxStatic(tile.box, BuildCraftLaserManager.STRIPES_READ, poseStack.last(), true);
        Minecraft.getInstance().getProfiler().pop();

//        RenderHelper.enableStandardItemLighting();
//        GL11.glPopMatrix();
        poseStack.popPose();

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
//    public boolean isGlobalRenderer(TileArchitectTable te)
    public boolean shouldRenderOffScreen(TileArchitectTable tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }
}
