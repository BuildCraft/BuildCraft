/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderPipeHolder implements BlockEntityRenderer<TilePipeHolder> {
    public RenderPipeHolder(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void renderTileEntityFast(TilePipeHolder pipe, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer)
    public void render(TilePipeHolder pipe, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());

        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("pipe");

        Minecraft.getInstance().getProfiler().push("wire");
        PipeWireRenderer.renderWires(pipe, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);

        Minecraft.getInstance().getProfiler().popPush("pluggable");
        renderPluggables(pipe, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);

        Minecraft.getInstance().getProfiler().popPush("contents");
        renderContents(pipe, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    // private static void renderPluggables(TilePipeHolder pipe, double x, double y, double z, float partialTicks, BufferBuilder bb)
    public void renderPluggables(TilePipeHolder pipe, float partialTicks, PoseStack poseStack, VertexConsumer buffer, int combinedLight, int combinedOverlay) {
        for (Direction face : Direction.values()) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug == null) {
                continue;
            }
            renderPlug(plug, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        }
    }

    private static <P extends PipePluggable> void renderPlug(P plug, float partialTicks, PoseStack poseStack, VertexConsumer buffer, int combinedLight, int combinedOverlay) {
        IPlugDynamicRenderer<P> renderer = PipeRegistryClient.getPlugRenderer(plug);
        if (renderer != null) {
            Minecraft.getInstance().getProfiler().push(plug.getClass().getName());
            renderer.render(plug, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    private static void renderContents(TilePipeHolder pipe, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Pipe p = pipe.getPipe();
        if (p == null) {
            return;
        }
        if (p.flow != null) {
            renderFlow(p.flow, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        }
        if (p.behaviour != null) {
            renderBehaviour(p.behaviour, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        }
    }

    private static <F extends PipeFlow> void renderFlow(F flow, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        IPipeFlowRenderer<F> renderer = PipeRegistryClient.getFlowRenderer(flow);
        if (renderer != null) {
            Minecraft.getInstance().getProfiler().push(flow.getClass().getName());
            renderer.render(flow, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    // private static <B extends PipeBehaviour> void renderBehaviour(B behaviour, double x, double y, double z, float partialTicks, BufferBuilder bb)
    private static <B extends PipeBehaviour> void renderBehaviour(B behaviour, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
        IPipeBehaviourRenderer<B> renderer = PipeRegistryClient.getBehaviourRenderer(behaviour);
        if (renderer != null) {
            Minecraft.getInstance().getProfiler().push(behaviour.getClass().getName());
//            renderer.render(behaviour, x, y, z, partialTicks, bb);
            renderer.render(behaviour, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
            Minecraft.getInstance().getProfiler().pop();
        }
    }
}
