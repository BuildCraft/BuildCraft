package buildcraft.lib.client.render.laser;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.*;

public abstract class LaserCompiledList {
    public abstract void render();

    public abstract void delete();

    public static class Builder implements ILaserRenderer {
        private final Tessellator tess;
        private final VertexBuffer buffer;
        private final boolean useColour;

        public Builder(boolean useNormalColour) {
            this.useColour = useNormalColour;
            tess = Tessellator.getInstance();
            buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, useNormalColour ? LaserRenderer_BC8.FORMAT_ALL : LaserRenderer_BC8.FORMAT_LESS);
        }

        @Override
        public void vertex(double x, double y, double z, double u, double v, int lmap, float nx, float ny, float nz, float diffuse) {
            buffer.pos(x, y, z);
            buffer.tex(u, v);
            buffer.lightmap((lmap >> 16) & 0xFFFF, lmap & 0xFFFF);
            if (useColour) {
                buffer.color(diffuse, diffuse, diffuse, 1.0f);
            }
            buffer.endVertex();
        }

        public LaserCompiledList build() {
            if (OpenGlHelper.useVbo()) {
                net.minecraft.client.renderer.vertex.VertexBuffer vb = new net.minecraft.client.renderer.vertex.VertexBuffer(this.buffer.getVertexFormat());
                buffer.finishDrawing();
                buffer.reset();
                vb.bufferData(buffer.getByteBuffer());
                return new Vbo(useColour, vb);
            } else {
                int glList = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(glList, GL11.GL_COMPILE);
                tess.draw();
                GL11.glEndList();
                return new GlList(glList);
            }
        }
    }

    private static class GlList extends LaserCompiledList {
        private final int glListId;

        private GlList(int glListId) {
            this.glListId = glListId;
        }

        @Override
        public void render() {
            GL11.glCallList(glListId);
        }

        @Override
        public void delete() {
            GL11.glDeleteLists(glListId, 1);
        }
    }

    private static class Vbo extends LaserCompiledList {
        private final boolean useColour;
        private final net.minecraft.client.renderer.vertex.VertexBuffer buffer;

        private Vbo(boolean useColour, net.minecraft.client.renderer.vertex.VertexBuffer buffer) {
            this.useColour = useColour;
            this.buffer = buffer;
        }

        @Override
        public void render() {
            final int stride = useColour ? 28 : 24;

            buffer.bindBuffer();
            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);

            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12);

            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, stride, 20);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

            if (useColour) {
                GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
                GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 24);
            }

            buffer.drawArrays(GL11.GL_QUADS);
            buffer.unbindBuffer();

            GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

            if (useColour) {
                GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                GlStateManager.color(1, 1, 1, 1);
            }
        }

        @Override
        public void delete() {
            buffer.deleteGlBuffers();
        }
    }
}
