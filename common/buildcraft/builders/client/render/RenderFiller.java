/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;

import buildcraft.builders.tile.TileFiller;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderFiller extends FastTESR<TileFiller> {
    @Override
    public void renderTileEntityFast(@Nonnull TileFiller tile, double x, double y, double z, float partialTicks,
        int destroyStage, @Nonnull VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("filler");

        Minecraft.getMinecraft().mcProfiler.startSection("main");
        RenderSnapshotBuilder.render(tile.builder, tile.getWorld(), tile.getPos(), x, y, z, partialTicks, vb);
        Minecraft.getMinecraft().mcProfiler.endSection();

        Minecraft.getMinecraft().mcProfiler.startSection("box");
//        if (tile.markerBox) {
            vb.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
            LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_WRITE, vb, true);
            vb.setTranslation(0, 0, 0);
//        }
        Minecraft.getMinecraft().mcProfiler.endSection();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller te) {
        return true;
    }
}
