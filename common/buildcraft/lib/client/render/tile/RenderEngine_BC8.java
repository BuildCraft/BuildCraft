/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.profiler.Profiler;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;

public abstract class RenderEngine_BC8<T extends TileEngineBase_BC8> extends FastTESR<T> {
    // TODO: Cache the model!


    @Override
    public void renderTileEntityFast(@Nonnull T engine, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("bc");
        profiler.startSection("engine");

        profiler.startSection("compute");
        vb.setTranslation(x, y, z);
        MutableQuad[] quads = getEngineModel(engine, partialTicks);
        profiler.endStartSection("render");
        MutableQuad copy = new MutableQuad(0, null);
        int lightc = engine.getWorld().getCombinedLight(engine.getPos(), 0);
        int light_block = (lightc >> 4) & 15;
        int light_sky = (lightc >> 20) & 15;
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            copy.maxLighti(light_block, light_sky);
            copy.multShade();
            copy.render(vb);
        }
        vb.setTranslation(0, 0, 0);

        profiler.endSection();
        profiler.endSection();
        profiler.endSection();
    }

    protected abstract MutableQuad[] getEngineModel(T engine, float partialTicks);
}
