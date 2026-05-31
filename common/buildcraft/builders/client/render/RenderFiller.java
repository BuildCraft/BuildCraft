/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;

import net.minecraftforge.client.model.animation.FastTESR;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;

import buildcraft.builders.tile.TileFiller;
import buildcraft.core.client.BuildCraftLaserManager;

@Environment(EnvType.CLIENT)
public class RenderFiller extends FastTESR<TileFiller> {

    @Override
    public void renderTileEntityFast(TileFiller tile, double x, double y, double z, float partialTicks,
        int destroyStage, float partial, BufferBuilder bb) {
        MinecraftClient.getInstance().getProfiler().push("bc");
        MinecraftClient.getInstance().getProfiler().push("filler");

        MinecraftClient.getInstance().getProfiler().push("main");
        if (tile.getBuilder() != null) {
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getWorld(), tile.getPos(), x, y, z, partialTicks, bb);
        }
        MinecraftClient.getInstance().getProfiler().pop();

        MinecraftClient.getInstance().getProfiler().push("box");
        if (tile.markerBox) {
            bb.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
            LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_WRITE, bb, true);
            bb.setTranslation(0, 0, 0);
        }
        MinecraftClient.getInstance().getProfiler().pop();

        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller te) {
        return true;
    }
}
