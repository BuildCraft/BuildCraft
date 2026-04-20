/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.laser;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.misc.RenderUtil;
import ct.buildcraft.lib.misc.RenderUtil.AutoTessellator;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class LaserCompiledList {
    public abstract void render(PoseStack pose, Matrix4f matrix);

    public abstract void delete();

    public static class Builder implements ILaserRenderer, AutoCloseable {
    	private final AutoTessellator tess;
        private final boolean useNormalColour;

        public Builder(boolean useNormalColour) {
            this.useNormalColour = useNormalColour;
            tess = RenderUtil.getThreadLocalUnusedTessellator();
            BufferBuilder bufferBuilder = tess.tessellator.getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, useNormalColour ? DefaultVertexFormat.BLOCK : DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }

        @Override
        public void vertex(float x, float y, float z, float u, float v, int lmap, float nx, float ny, float nz,
            float diffuse) {
        	BufferBuilder bufferBuilder = tess.tessellator.getBuilder();
            bufferBuilder.vertex(x, y, z);
            if (useNormalColour) 
                bufferBuilder.color(diffuse, diffuse, diffuse, 1.0f);
            else
            	bufferBuilder.color(1.0f, 1.0f, 1.0f, 1.0f);
            bufferBuilder.uv(u, v);
            bufferBuilder.uv2((lmap >> 16) & 0xFFFF, lmap & 0xFFFF);
            if(useNormalColour)
            	bufferBuilder.normal(nx, ny, nz);
            bufferBuilder.endVertex();
        }

        public LaserCompiledList build() {
        	BufferBuilder bufferBuilder = tess.tessellator.getBuilder();
        	VertexBuffer vertexBuffer = new VertexBuffer();
        	vertexBuffer.bind();
        	vertexBuffer.upload(bufferBuilder.end());
        	VertexBuffer.unbind();
        	return new Vbo(useNormalColour, vertexBuffer);
        }

        @Override
        public void close() {
            tess.close();
        }
    }

    private static class Vbo extends LaserCompiledList {
        private final boolean useNormalColour;
        private final VertexBuffer vertexBuffer;

        private Vbo(boolean useColour, VertexBuffer vertexBuffer) {
            this.useNormalColour = useColour;
            this.vertexBuffer = vertexBuffer;
        }

        @Override
        public void render(PoseStack pose, Matrix4f matrix) {
            vertexBuffer.bind();
            RenderSystem.setShader(useNormalColour ? GameRenderer::getBlockShader : GameRenderer::getPositionColorTexLightmapShader);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            
            ShaderInstance shaderinstance = RenderSystem.getShader();
            vertexBuffer.drawWithShader(pose.last().pose(), matrix, shaderinstance);
            
            VertexBuffer.unbind();

        }

        @Override
        public void delete() {
        	vertexBuffer.close();
        }
    }
}
