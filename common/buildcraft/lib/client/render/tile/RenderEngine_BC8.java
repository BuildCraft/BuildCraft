/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import javax.annotation.Nonnull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.profiler.Profiler;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;

public abstract class RenderEngine_BC8<T extends TileEngineBase_BC8> extends FastTESR<T> {
    // TODO: Cache the model!

    @Override
    public void renderTileEntityFast(@Nonnull T engine, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder vb) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("bc");
        profiler.push("engine");

        profiler.push("compute");
        vb.setTranslation(x, y, z);
        MutableQuad[] quads = getEngineModel(engine, partialTicks);
        profiler.swap("render");
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

        profiler.pop();
        profiler.pop();
        profiler.pop();
    }

    protected abstract MutableQuad[] getEngineModel(T engine, float partialTicks);
}
