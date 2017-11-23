/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderArchitectTable extends TileEntitySpecialRenderer<TileArchitectTable> {
    @Override
    public void render(TileArchitectTable tile, double x, double y, double z, float partialTicks, int destroyStage,
        float partial) {
        if (!tile.markerBox) {
            return;
        }
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("architect_table");

        GL11.glPushMatrix();
        GL11.glTranslated(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
        RenderHelper.disableStandardItemLighting();

        Minecraft.getMinecraft().mcProfiler.startSection("box");
        LaserBoxRenderer.renderLaserBoxStatic(tile.box, BuildCraftLaserManager.STRIPES_READ, true);
        Minecraft.getMinecraft().mcProfiler.endSection();

        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileArchitectTable te) {
        return true;
    }
}
