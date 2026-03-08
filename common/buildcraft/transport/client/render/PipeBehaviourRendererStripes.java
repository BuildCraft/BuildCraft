/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PipeBehaviourRendererStripes implements IPipeBehaviourRenderer<PipeBehaviourStripes> {
    INSTANCE;

    @Override
//    public void render(PipeBehaviourStripes stripes, double x, double y, double z, float partialTicks, BufferBuilder bb)
    public void render(PipeBehaviourStripes stripes, float partialTicks, PoseStack poseStack, VertexConsumer bb, int combinedLight, int combinedOverlay) {
        Direction dir = stripes.direction;
        if (dir == null) return;
        MutableQuad[] quads = BCTransportModels.getStripesDynQuads(dir);
//        bb.setTranslation(x, y, z);
//        int light = stripes.pipe.getHolder().getPipeWorld().getCombinedLight(stripes.pipe.getHolder().getPipePos(), 0);
        int light = RenderUtil.getCombinedLight(stripes.pipe.getHolder().getPipeWorld(), stripes.pipe.getHolder().getPipePos());
        for (MutableQuad q : quads) {
            q.multShade();
            q.lighti(light);
            q.overlay(combinedOverlay);
            q.render(poseStack.last(), bb);
        }
//        bb.setTranslation(0, 0, 0);
    }
}
