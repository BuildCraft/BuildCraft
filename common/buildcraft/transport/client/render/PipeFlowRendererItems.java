/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.transport.pipe.flow.PipeFlowItems;

// STUB(R.Chen): PipeFlowRendererItems — Fabric VertexConsumer skeleton. Full item-in-transit
// rendering deferred to Phase 5 (blocked by ItemRenderUtil Forge migration).
@Environment(EnvType.CLIENT)
public enum PipeFlowRendererItems implements IPipeFlowRenderer<PipeFlowItems> {
    INSTANCE;

    @Override
    public void render(PipeFlowItems flow, MatrixStack matrices, VertexConsumer vc,
            int light, float partialTicks) {
        // STUB(R.Chen): Phase 5 — ItemRenderUtil needs Fabric port before item-in-pipe rendering.
    }
}
