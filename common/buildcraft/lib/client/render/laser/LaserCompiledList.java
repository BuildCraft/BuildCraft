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
        private final boolean useNormalColour;

        public Builder(boolean useNormalColour) {
            this.useNormalColour = useNormalColour;
            tess = Tessellator.getInstance();
            buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, useNormalColour ? LaserRenderer_BC8.FORMAT_ALL : LaserRenderer_BC8.FORMAT_LESS);
        }

        @Override
        public void vertex(double x, double y, double z, double u, double v, int lmap, float nx, float ny, float nz, float diffuse) {
            buffer.pos(x, y, z);
            buffer.tex(u, v);
            buffer.lightmap((lmap >> 16) & 0xFFFF, lmap & 0xFFFF);
            if (useNormalColour) {
                buffer.color(diffuse, diffuse, diffuse, 1.0f);
                buffer.normal(nx, ny, nz);
            }
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
