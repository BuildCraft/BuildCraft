package buildcraft.lib.client.render.laser;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;

public class LaserCompiledList {
    private final int glList;

    public LaserCompiledList(int glList) {
        this.glList = glList;
    }

    public void render() {
        GL11.glCallList(glList);
    }

    public void delete() {
        GLAllocation.deleteDisplayLists(glList);
    }

    public static class Builder implements ILaserRenderer {
        private final Tessellator tess;
        private final VertexBuffer buffer;

        public Builder() {
            tess = Tessellator.getInstance();
            buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, LaserRenderer_BC8.POSITION_TEX_LMAP);
        }

        @Override
        public void vertex(double x, double y, double z, double u, double v, int lmap) {
            buffer.pos(x, y, z);
            buffer.tex(u, v);
            buffer.lightmap(lmap >> 16 & 65535, lmap & 65535);
            buffer.endVertex();
        }

        public LaserCompiledList build() {
            int glList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glList, GL11.GL_COMPILE);
            tess.draw();
            GL11.glEndList();
            return new LaserCompiledList(glList);
        }
    }
}
