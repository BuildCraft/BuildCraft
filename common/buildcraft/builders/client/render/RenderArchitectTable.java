/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderArchitectTable extends TileEntitySpecialRenderer<TileArchitectTable> {
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void render(TileArchitectTable tile, double x, double y, double z, float partialTicks, int destroyStage,
        float partial) {
        if (!tile.markerBox) {
            return;
        }
        MinecraftClient.getInstance().getProfiler().push("bc");
        MinecraftClient.getInstance().getProfiler().push("architect_table");

        GL11.glPushMatrix();
        GL11.glTranslated(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
        RenderHelper.disableStandardItemLighting();

        MinecraftClient.getInstance().getProfiler().push("box");
        LaserBoxRenderer.renderLaserBoxStatic(tile.box, BuildCraftLaserManager.STRIPES_READ, true);
        MinecraftClient.getInstance().getProfiler().pop();

        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();

        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isGlobalRenderer(TileArchitectTable te) {
        return true;
    }
}
