package buildcraft.core.lib.render;

import java.util.Arrays;

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

import buildcraft.core.lib.EntityResizableCube;

public class RenderResizableCube extends Render {
    public static final RenderResizableCube INSTANCE = new RenderResizableCube();

    private static final int U_MIN = 0;
    private static final int U_MAX = 1;
    private static final int V_MIN = 2;
    private static final int V_MAX = 3;

    protected RenderResizableCube() {
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
        renderCube((EntityResizableCube) entity);
        GL11.glTranslated(-x, -y, -z);
        GL11.glPopMatrix();
    }

    private void renderCube(EntityResizableCube cube) {
        TextureAtlasSprite[] sprites = cube.textures;
        if (sprites == null) {
            sprites = new TextureAtlasSprite[6];
            for (int i = 0; i < 6; i++) {
                sprites[i] = cube.texture;
            }
        }

        int[] rotations = cube.textureRotations;
        if (rotations == null) {
            rotations = new int[6];
        }

        boolean[] flips = cube.textureFlips;
        if (flips == null) {
            flips = new boolean[6];
        }

        double textureX = cube.textureXSize / 16D;
        double textureY = cube.textureYSize / 16D;
        double textureZ = cube.textureZSize / 16D;

        double sizeX = cube.iSize;
        double sizeY = cube.jSize;
        double sizeZ = cube.kSize;

        bindTexture(cube.resource == null ? TextureMap.locationBlocksTexture : cube.resource);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        wr.startDrawingQuads();

        if (sprites[0] != null) {
            // Down
            float[] uv = getUVArray(sprites[0], rotations[0], flips[0]);

            for (float x = 0; x < sizeX; x += textureX) {
                for (float z = 0; z < sizeZ; z += textureZ) {
                    float[] uvT = Arrays.copyOf(uv, 4);

                    double addX = textureX;
                    double addZ = textureZ;
                    if (x + addX > sizeX) {
                        addX = sizeX - x;
                        float uDiff = uvT[U_MAX] - uvT[U_MIN];
                        uvT[U_MAX] = uvT[U_MIN] + uDiff * (float) (addX / textureX);
                    }

                    if (z + addZ > sizeZ) {
                        addZ = sizeZ - z;
                        float vDiff = uvT[V_MAX] - uvT[V_MIN];
                        uvT[V_MAX] = uvT[V_MIN] + vDiff * (float) (addZ / textureZ);
                    }

                    // @formatter:off
                    wr.addVertexWithUV(x + addX, 0, z,        uvT[U_MAX], uvT[V_MIN]);
                    wr.addVertexWithUV(x + addX, 0, z + addZ, uvT[U_MAX], uvT[V_MAX]);
                    wr.addVertexWithUV(x,        0, z + addZ, uvT[U_MIN], uvT[V_MAX]);
                    wr.addVertexWithUV(x,        0, z,        uvT[U_MIN], uvT[V_MIN]);
                    // @formatter:on
                }
            }
        }

        if (sprites[1] != null) {
            // Up
            float[] uv = getUVArray(sprites[0], rotations[0], flips[0]);

            for (float x = 0; x < sizeX; x += textureX) {
                for (float z = 0; z < sizeZ; z += textureZ) {
                    float[] uvT = Arrays.copyOf(uv, 4);

                    double addX = textureX;
                    double addZ = textureZ;
                    if (x + addX > sizeX) {
                        addX = sizeX - x;
                        float uDiff = uvT[U_MAX] - uvT[U_MIN];
                        uvT[U_MAX] = uvT[U_MIN] + uDiff * (float) (addX / textureX);
                    }

                    if (z + addZ > sizeZ) {
                        addZ = sizeZ - z;
                        float vDiff = uvT[V_MAX] - uvT[V_MIN];
                        uvT[V_MAX] = uvT[V_MIN] + vDiff * (float) (addZ / textureZ);
                    }

                    // @formatter:off
                    wr.addVertexWithUV(x,        sizeY, z,        uvT[U_MIN], uvT[V_MIN]);
                    wr.addVertexWithUV(x,        sizeY, z + addZ, uvT[U_MIN], uvT[V_MAX]);
                    wr.addVertexWithUV(x + addX, sizeY, z + addZ, uvT[U_MAX], uvT[V_MAX]);
                    wr.addVertexWithUV(x + addX, sizeY, z,        uvT[U_MAX], uvT[V_MIN]);
                    // @formatter:on
                }
            }
        }

        if (sprites[2] != null) {
            // North (-z)
        }

        if (sprites[3] != null) {
            // South (+Z)
        }

        if (sprites[4] != null) {
            // West (-X)
        }

        if (sprites[5] != null) {
            // East (+X)
        }

        tess.draw();

        GlStateManager.disableAlpha();
    }

    /** Returns an array containing [uMin, uMax, vMin, vMax] */
    private float[] getUVArray(TextureAtlasSprite sprite, int rotation, boolean flip) {
        float[] uvarray = new float[] { sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV() };
        for (int i = 0; i < rotation; i++) {
            float holder = uvarray[0];
            uvarray[0] = uvarray[1];
            uvarray[1] = uvarray[2];
            uvarray[2] = uvarray[3];
            uvarray[3] = holder;
        }

        if (flip) {
            float holder = uvarray[0];
            uvarray[0] = uvarray[2];
            uvarray[2] = holder;
            holder = uvarray[1];
            uvarray[1] = uvarray[3];
            uvarray[3] = holder;
        }
        return uvarray;
    }
}
