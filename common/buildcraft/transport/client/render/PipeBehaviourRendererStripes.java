/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;

import buildcraft.lib.client.model.MutableQuad;

import buildcraft.transport.BCTransportModels;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;

@SideOnly(Side.CLIENT)
public enum PipeBehaviourRendererStripes implements IPipeBehaviourRenderer<PipeBehaviourStripes> {
    INSTANCE;

    @Override
    public void render(PipeBehaviourStripes stripes, double x, double y, double z, float partialTicks, BufferBuilder bb) {
        EnumFacing dir = stripes.direction;
        if (dir == null) return;
        MutableQuad[] quads = BCTransportModels.getStripesDynQuads(dir);
        bb.setTranslation(x, y, z);
        int light = stripes.pipe.getHolder().getPipeWorld().getCombinedLight(stripes.pipe.getHolder().getPipePos(), 0);
        for (MutableQuad q : quads) {
            q.multShade();
            q.lighti(light);
            q.render(bb);
        }
        bb.setTranslation(0, 0, 0);
    }
}
