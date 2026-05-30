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

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;

@Environment(EnvType.CLIENT)
public class RenderPipeHolder implements BlockEntityRenderer<TilePipeHolder> {

    public RenderPipeHolder(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(TilePipeHolder entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        VertexConsumer cutout = vertexConsumers.getBuffer(RenderLayer.getCutout());

        PipeWireRenderer.renderWires(entity, matrices, cutout, light);
        renderPluggables(entity, matrices, cutout, light, tickDelta);
        renderContents(entity, matrices, cutout, light, tickDelta);
    }

    private static void renderPluggables(TilePipeHolder pipe, MatrixStack matrices,
            VertexConsumer vc, int light, float tickDelta) {
        for (Direction face : Direction.values()) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug == null) continue;
            renderPlug(plug, matrices, vc, light, tickDelta);
        }
    }

    @SuppressWarnings("unchecked")
    private static <P extends PipePluggable> void renderPlug(P plug, MatrixStack matrices,
            VertexConsumer vc, int light, float tickDelta) {
        IPlugDynamicRenderer<P> renderer =
            (IPlugDynamicRenderer<P>) PipeRegistryClient.getPlugRenderer(plug);
        if (renderer != null) {
            renderer.render(plug, matrices, vc, light, tickDelta);
        }
    }

    private static void renderContents(TilePipeHolder pipe, MatrixStack matrices,
            VertexConsumer vc, int light, float tickDelta) {
        Pipe p = pipe.getPipe();
        if (p == null) return;
        if (p.flow != null) renderFlow(p.flow, matrices, vc, light, tickDelta);
        if (p.behaviour != null) renderBehaviour(p.behaviour, matrices, vc, light, tickDelta);
    }

    @SuppressWarnings("unchecked")
    private static <F extends PipeFlow> void renderFlow(F flow, MatrixStack matrices,
            VertexConsumer vc, int light, float tickDelta) {
        IPipeFlowRenderer<F> renderer =
            (IPipeFlowRenderer<F>) PipeRegistryClient.getFlowRenderer(flow);
        if (renderer != null) {
            renderer.render(flow, matrices, vc, light, tickDelta);
        }
    }

    @SuppressWarnings("unchecked")
    private static <B extends PipeBehaviour> void renderBehaviour(B behaviour, MatrixStack matrices,
            VertexConsumer vc, int light, float tickDelta) {
        IPipeBehaviourRenderer<B> renderer =
            (IPipeBehaviourRenderer<B>) PipeRegistryClient.getBehaviourRenderer(behaviour);
        if (renderer != null) {
            renderer.render(behaviour, matrices, vc, light, tickDelta);
        }
    }
}
