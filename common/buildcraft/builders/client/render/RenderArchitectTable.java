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

import buildcraft.lib.client.render.laser.RenderLaserBox;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderArchitectTable extends FastTESR<TileArchitectTable> {
    @Override
    public void renderTileEntityFast(@Nonnull TileArchitectTable tile, double x, double y, double z, float partialTicks, int destroyStage, @Nonnull VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("architect_table");

        Minecraft.getMinecraft().mcProfiler.startSection("box");
        if (tile.markerBox) {
            vb.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
            RenderLaserBox.renderDynamic(tile.box, BuildCraftLaserManager.STRIPES_READ, vb, false);
            vb.setTranslation(0, 0, 0);
        }
        Minecraft.getMinecraft().mcProfiler.endSection();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileArchitectTable te) {
        return true;
    }
}
