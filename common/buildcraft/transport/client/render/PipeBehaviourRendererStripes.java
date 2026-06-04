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

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;

import buildcraft.lib.client.model.MutableQuad;

import buildcraft.transport.BCTransportModels;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;

@Environment(EnvType.CLIENT)
public enum PipeBehaviourRendererStripes implements IPipeBehaviourRenderer<PipeBehaviourStripes> {
    INSTANCE;

    @Override
    public void render(PipeBehaviourStripes stripes, MatrixStack matrices, VertexConsumer vc,
            int light, float partialTicks) {
        if (stripes.direction == null) return;
        // TODO(R.Chen): BCTransportModels.getStripesDynQuads deferred to Phase 5 FRAPI model layer.
        // MutableQuad[] quads = BCTransportModels.getStripesDynQuads(stripes.direction);
        // if (quads == null) return;
        // int blockLight = light & 0xFFFF;
        // int skyLight = (light >> 16) & 0xFFFF;
        // for (MutableQuad q : quads) {
        //     MutableQuad copy = new MutableQuad(q);
        //     copy.setCalculatedDiffuse();
        //     copy.lighti(blockLight, skyLight);
        //     copy.render(vc);
        // }
    }
}
