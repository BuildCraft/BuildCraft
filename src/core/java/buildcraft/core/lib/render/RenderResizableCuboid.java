package buildcraft.core.lib.render;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.EntityResizableCuboid;

public class RenderResizableCuboid extends Render {
    public static final RenderResizableCuboid INSTANCE = new RenderResizableCuboid();

    private static final int U_MIN = 0;
    private static final int U_MAX = 1;
    private static final int V_MIN = 2;
    private static final int V_MAX = 3;

    protected RenderResizableCuboid() {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        renderCube((EntityResizableCuboid) entity);
        GL11.glTranslated(-x, -y, -z);
        GL11.glPopMatrix();
    }

    private void renderCube(EntityResizableCuboid cube) {
        TextureAtlasSprite[] sprites = cube.textures;
        if (sprites == null) {
            sprites = new TextureAtlasSprite[6];
            for (int i = 0; i < 6; i++) {
                sprites[i] = cube.texture;
            }
        }

        int[] flips = cube.textureFlips;
        if (flips == null) {
            flips = new int[6];
        }

        double textureStartX = cube.textureStartX / 16D;
        double textureStartY = cube.textureStartY / 16D;
        double textureStartZ = cube.textureStartZ / 16D;

        double textureSizeX = cube.textureSizeX / 16D;
        double textureSizeY = cube.textureSizeY / 16D;
        double textureSizeZ = cube.textureSizeZ / 16D;

        double textureEndX = textureSizeX + textureStartX;
        double textureEndY = textureSizeY + textureStartY;
        double textureEndZ = textureSizeZ + textureStartZ;

        double textureOffsetX = cube.textureOffsetX / 16D;
        double textureOffsetY = cube.textureOffsetY / 16D;
        double textureOffsetZ = cube.textureOffsetZ / 16D;

        double sizeX = cube.iSize;
        double sizeY = cube.jSize;
        double sizeZ = cube.kSize;

        bindTexture(cube.resource == null ? TextureMap.locationBlocksTexture : cube.resource);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableLighting();

        wr.startDrawingQuads();

        if (sprites[0] != null) {
            // Down
            float[] uv = getUVArray(sprites[0], flips[0], textureStartX, textureEndX, textureStartZ, textureEndZ);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                wr.addVertexWithUV(ri.xyz[U_MAX], 0, ri.xyz[V_MIN], ri.uv[U_MAX], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MAX], 0, ri.xyz[V_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], 0, ri.xyz[V_MAX], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], 0, ri.xyz[V_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);

                wr.addVertexWithUV(ri.xyz[U_MIN], 0, ri.xyz[V_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MIN], 0, ri.xyz[V_MAX], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], 0, ri.xyz[V_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], 0, ri.xyz[V_MIN], ri.uv[U_MAX], ri.uv[V_MIN]);
            }
        }

        if (sprites[1] != null) {
            // Up
            float[] uv = getUVArray(sprites[1], flips[1], textureStartX, textureEndX, textureStartZ, textureEndZ);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                wr.addVertexWithUV(ri.xyz[U_MAX], sizeY, ri.xyz[V_MIN], ri.uv[U_MAX], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MAX], sizeY, ri.xyz[V_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], sizeY, ri.xyz[V_MAX], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], sizeY, ri.xyz[V_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);

                wr.addVertexWithUV(ri.xyz[U_MIN], sizeY, ri.xyz[V_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MIN], sizeY, ri.xyz[V_MAX], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], sizeY, ri.xyz[V_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], sizeY, ri.xyz[V_MIN], ri.uv[U_MAX], ri.uv[V_MIN]);
            }
        }

        if (sprites[2] != null) {
            // North (-Z)
            float[] uv = getUVArray(sprites[2], flips[2], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MIN], 0, ri.uv[U_MAX], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MAX], 0, ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MAX], 0, ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MIN], 0, ri.uv[U_MIN], ri.uv[V_MIN]);

                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MIN], 0, ri.uv[U_MIN], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MAX], 0, ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MAX], 0, ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MIN], 0, ri.uv[U_MAX], ri.uv[V_MIN]);
            }
        }

        if (sprites[3] != null) {
            // South (+Z)
            float[] uv = getUVArray(sprites[3], flips[3], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MIN], sizeZ, ri.uv[U_MAX], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MAX], sizeZ, ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MAX], sizeZ, ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MIN], sizeZ, ri.uv[U_MIN], ri.uv[V_MIN]);

                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MIN], sizeZ, ri.uv[U_MIN], ri.uv[V_MIN]);
                wr.addVertexWithUV(ri.xyz[U_MIN], ri.xyz[V_MAX], sizeZ, ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MAX], sizeZ, ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(ri.xyz[U_MAX], ri.xyz[V_MIN], sizeZ, ri.uv[U_MAX], ri.uv[V_MIN]);
            }
        }

        if (sprites[4] != null) {
            // West (-X)
            float[] uv = getUVArray(sprites[4], flips[4], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                wr.addVertexWithUV(0, ri.xyz[V_MIN], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MIN]);
                wr.addVertexWithUV(0, ri.xyz[V_MAX], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(0, ri.xyz[V_MAX], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(0, ri.xyz[V_MIN], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);

                wr.addVertexWithUV(0, ri.xyz[V_MIN], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);
                wr.addVertexWithUV(0, ri.xyz[V_MAX], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(0, ri.xyz[V_MAX], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(0, ri.xyz[V_MIN], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MIN]);
            }
        }

        if (sprites[5] != null) {
            // East (+X)
            float[] uv = getUVArray(sprites[5], flips[5], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                wr.addVertexWithUV(sizeX, ri.xyz[V_MIN], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MIN]);
                wr.addVertexWithUV(sizeX, ri.xyz[V_MAX], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(sizeX, ri.xyz[V_MAX], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(sizeX, ri.xyz[V_MIN], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);

                wr.addVertexWithUV(sizeX, ri.xyz[V_MIN], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MIN]);
                wr.addVertexWithUV(sizeX, ri.xyz[V_MAX], ri.xyz[U_MIN], ri.uv[U_MIN], ri.uv[V_MAX]);
                wr.addVertexWithUV(sizeX, ri.xyz[V_MAX], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MAX]);
                wr.addVertexWithUV(sizeX, ri.xyz[V_MIN], ri.xyz[U_MAX], ri.uv[U_MAX], ri.uv[V_MIN]);
            }
        }

        tess.draw();

        GlStateManager.disableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableFog();
    }

    /** Returns an array containing [uMin, uMax, vMin, vMax]. start* and end* must be doubles between 0 and 1 */
    private float[] getUVArray(TextureAtlasSprite sprite, int flips, double startU, double endU, double startV, double endV) {
        float minU = sprite.getInterpolatedU(startU * 16);
        float maxU = sprite.getInterpolatedU(endU * 16);
        float minV = sprite.getInterpolatedV(startV * 16);
        float maxV = sprite.getInterpolatedV(endV * 16);
        float[] uvarray = new float[] { minU, maxU, minV, maxV };
        if (flips % 2 == 1) {
            float holder = uvarray[0];
            uvarray[0] = uvarray[1];
            uvarray[1] = holder;
        }
        if (flips >> 1 % 2 == 1) {
            float holder = uvarray[2];
            uvarray[2] = uvarray[3];
            uvarray[3] = holder;
        }
        return uvarray;
    }

    /** A way to automatically generate the different positions given the same arguments. */
    private List<RenderInfo> getRenderInfos(float[] uv, double sizeU, double sizeV, double textureSizeU, double textureSizeV, double textureOffsetU,
            double textureOffsetV) {
        List<RenderInfo> infos = Lists.newArrayList();
        boolean firstU = true;
        for (double u = 0; u < sizeU; u += textureSizeU) {
            boolean firstV = true;
            for (double v = 0; v < sizeV; v += textureSizeV) {
                float[] uvt = Arrays.copyOf(uv, 4);

                double addU = textureSizeU;
                double addV = textureSizeV;

                boolean lowerU = false;
                boolean lowerV = false;

                // If there is an offset then make sure the texture positions are changed properly
                if (firstU && textureOffsetU != 0) {
                    uvt[U_MIN] = uvt[U_MIN] + (uvt[U_MAX] - uvt[U_MIN]) * (float) textureOffsetU;
                    addU -= 1 - textureOffsetU;
                    lowerU = true;
                }
                firstU = false;

                if (firstV && textureOffsetV != 0) {
                    uvt[V_MIN] = uvt[V_MIN] + (uvt[V_MAX] - uvt[V_MIN]) * (float) textureOffsetV;
                    addV -= 1 - textureOffsetV;
                    lowerV = true;
                }
                firstV = false;

                // If the size of the texture is greater than the cuboid goes on for then make sure the texture
                // positions are lowered
                if (u + addU > sizeU) {
                    addU = sizeU - u;
                    float uDiff = uvt[U_MAX] - uvt[U_MIN];
                    uvt[U_MAX] = uvt[U_MIN] + uDiff * (float) (addU / textureSizeU);
                }

                if (v + addV > sizeV) {
                    addV = sizeV - v;
                    float vDiff = uvt[V_MAX] - uvt[V_MIN];
                    uvt[V_MAX] = uvt[V_MIN] + vDiff * (float) (addV / textureSizeV);
                }

                double[] xyz = new double[4];
                xyz[U_MIN] = u;
                xyz[U_MAX] = u + addU;
                xyz[V_MIN] = v;
                xyz[V_MAX] = v + addV;
                infos.add(new RenderInfo(uvt, xyz));

                // If we lowered the U or V because the cuboid started on an offset, reset it back to what was actually
                // rendered, not what the for loop assumes
                if (lowerU) {
                    u -= textureSizeU;
                    u += addU;
                }

                if (lowerV) {
                    v -= textureSizeV;
                    v += addU;
                }
            }
        }
        return infos;
    }

    private static final class RenderInfo {
        private final float[] uv;
        private final double[] xyz;

        public RenderInfo(float[] uv, double[] xyz) {
            this.uv = uv;
            this.xyz = xyz;
        }
    }
}
