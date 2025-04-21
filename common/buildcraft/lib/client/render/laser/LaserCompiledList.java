/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public abstract class LaserCompiledList {
    private static final RenderType LASER_RENDER_TYPE_FORMAT_ALL = RenderType.create(
            "buildcraft_laser_all",
            LaserRenderer_BC8.FORMAT_ALL, GL11.GL_QUADS,
            256,
            false,
            true,
            RenderType.State.builder()
                    .setLightmapState(RenderState.LIGHTMAP)
                    .setTextureState(RenderState.BLOCK_SHEET)
                    .createCompositeState(true)
    );

    public abstract void render(MatrixStack.Entry modelViewMatrix);

    public abstract void delete();

    public static class Builder implements ILaserRenderer, AutoCloseable {
        public final RenderUtil.AutoTessellator tess;
        // private final boolean useColour;

        // public Builder(boolean useNormalColour)
        public Builder() {
//            this.useColour = useNormalColour;
            tess = RenderUtil.getThreadLocalUnusedTessellator();
            BufferBuilder bufferBuilder = tess.tessellator.getBuilder();
//            bufferBuilder.begin(VertexFormat.Mode.QUADS, useNormalColour ? LaserRenderer_BC8.FORMAT_ALL : LaserRenderer_BC8.FORMAT_LESS);
            bufferBuilder.begin(GL11.GL_QUADS, LaserRenderer_BC8.FORMAT_ALL);
        }

        @Override
        public void vertex(
                double x, double y, double z,
                double u, double v,
                int lmap,
                int overlay,
                float nx,
                float ny,
                float nz,
                float diffuse
        ) {
//            BufferBuilder bufferBuilder = tess.tessellator.getBuffer();
            BufferBuilder bufferBuilder = tess.tessellator.getBuilder();
            // bufferBuilder.pos(x, y, z);
            bufferBuilder.vertex(x, y, z);
//            if (useColour) {
            bufferBuilder.color(diffuse, diffuse, diffuse, 1.0f);
//            }
            // bufferBuilder.tex(u, v);
            bufferBuilder.uv((float) u, (float) v);
//            bufferBuilder.overlayCoords(overlay);
            // bufferBuilder.lightmap((lmap >> 16) & 0xFFFF, lmap & 0xFFFF);
            bufferBuilder.uv2(lmap);
//            bufferBuilder.normal(nx, ny, nz);
            bufferBuilder.endVertex();
        }

        public LaserCompiledList build() {
//            if (OpenGlHelper.useVbo()) {
//            BufferBuilder bufferBuilder = tess.tessellator.getBuffer();
            BufferBuilder bufferBuilder = tess.tessellator.getBuilder();
//            VertexBuffer vertexBuffer = new VertexBuffer(bufferBuilder.getVertexFormat());
            VertexBuffer vertexBuffer = new VertexBuffer(LaserRenderer_BC8.FORMAT_ALL);
//            bufferBuilder.finishDrawing();
//            bufferBuilder.reset();
            bufferBuilder.end();
//            vertexBuffer.bufferData(bufferBuilder.getByteBuffer());
            vertexBuffer.upload(bufferBuilder);
//            return new Vbo(useColour, vertexBuffer);
            return new Vbo(vertexBuffer);
//            } else {
//                int glList = GLAllocation.generateDisplayLists(1);
//                GL11.glNewList(glList, GL11.GL_COMPILE);
//                tess.tessellator.draw();
//                GL11.glEndList();
//                return new GlList(glList);
//            }
        }

        @Override
        public void close() {
            tess.close();
        }
    }

//    private static class GlList extends LaserCompiledList {
//        private final int glListId;
//
//        private GlList(int glListId) {
//            this.glListId = glListId;
//        }
//
//        @Override
//        public void render() {
//            GL11.glCallList(glListId);
//        }
//
//        @Override
//        public void delete() {
//            GL11.glDeleteLists(glListId, 1);
//        }
//    }

    private static class Vbo extends LaserCompiledList {
        // private final boolean useColour;
        private final VertexBuffer vertexBuffer;

        // private Vbo(boolean useColour, VertexBuffer vertexBuffer)
        private Vbo(VertexBuffer vertexBuffer) {
//            this.useColour = useColour;
            this.vertexBuffer = vertexBuffer;
        }

        @Override
        public void render(MatrixStack.Entry modelViewMatrix) {
            LASER_RENDER_TYPE_FORMAT_ALL.setupRenderState();
            RenderSystem.color4f(1, 1, 1, 1);
            vertexBuffer.bind();
            LaserRenderer_BC8.FORMAT_ALL.setupBufferState(0L);
            RenderSystem.matrixMode(GL11.GL_MODELVIEW);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(modelViewMatrix.pose());
            vertexBuffer.draw(modelViewMatrix.pose(), GL11.GL_QUADS);
            VertexBuffer.unbind();
            RenderSystem.popMatrix();
            LaserRenderer_BC8.FORMAT_ALL.clearBufferState();
            LASER_RENDER_TYPE_FORMAT_ALL.clearRenderState();
        }

        @Override
        public void delete() {
//            vertexBuffer.deleteGlBuffers();
            vertexBuffer.close();
        }
    }
}
