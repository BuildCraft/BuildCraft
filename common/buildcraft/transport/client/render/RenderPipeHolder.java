/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;

public class RenderPipeHolder extends FastTESR<TilePipeHolder> {
    @Override
    public void renderTileEntityFast(@Nonnull TilePipeHolder pipe, double x, double y, double z, float partialTicks, int destroyStage, @Nonnull VertexBuffer vb) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("pipe");

        Minecraft.getMinecraft().mcProfiler.startSection("wire");
        PipeWireRenderer.renderWires(pipe, x, y, z, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("pluggable");
        renderPluggables(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("contents");
        renderContents(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderPluggables(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug == null) {
                continue;
            }
            renderPlug(plug, x, y, z, partialTicks, vb);
        }
    }

    private static <P extends PipePluggable> void renderPlug(P plug, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        IPlugDynamicRenderer<P> renderer = PipeRegistryClient.getPlugRenderer(plug);
        if (renderer != null) {
            renderer.render(plug, x, y, z, partialTicks, vb);
        }
    }

    private static void renderContents(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        Pipe p = pipe.getPipe();
        if (p == null) {
            return;
        }
        if (p.flow != null) {
            renderFlow(p.flow, x, y, z, partialTicks, vb);
        }
        if (p.behaviour != null) {
            renderBehaviour(p.behaviour, x, y, z, partialTicks, vb);
        }
    }

    private static <F extends PipeFlow> void renderFlow(F flow, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        IPipeFlowRenderer<F> renderer = PipeRegistryClient.getFlowRenderer(flow);
        if (renderer != null) {
            renderer.render(flow, x, y, z, partialTicks, vb);
        }
    }

    private static <B extends PipeBehaviour> void renderBehaviour(B behaviour, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        IPipeBehaviourRenderer<B> renderer = PipeRegistryClient.getBehaviourRenderer(behaviour);
        if (renderer != null) {
            renderer.render(behaviour, x, y, z, partialTicks, vb);
        }
    }
}
