/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

public enum PipeBehaviourRendererStripes implements IPipeBehaviourRenderer<PipeBehaviourStripes> {
    INSTANCE;

    @Override
    public void render(PipeBehaviourStripes stripes, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        EnumFacing dir = stripes.direction;
        if (dir == null) return;
        MutableQuad[] quads = BCTransportModels.getStripesDynQuads(dir);
        vb.setTranslation(x, y, z);
        int light = stripes.pipe.getHolder().getPipeWorld().getCombinedLight(stripes.pipe.getHolder().getPipePos(), 0);
        for (MutableQuad q : quads) {
            q.multShade();
            q.lighti(light);
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}
