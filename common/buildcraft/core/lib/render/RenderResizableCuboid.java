package buildcraft.core.lib.render;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
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
    public boolean shouldRender(Entity entity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        renderCube((EntityResizableCuboid) entity);
        GL11.glTranslated(-x, -y, -z);
        GL11.glPopMatrix();
    }

    /** This will render a cuboid from its middle. */
    public void renderCubeFromCentre(EntityResizableCuboid cuboid) {
        GL11.glPushMatrix();
        GL11.glTranslated(-cuboid.xSize / 2d, -cuboid.ySize / 2d, -cuboid.zSize / 2d);
        renderCube(cuboid);
        GL11.glPopMatrix();
    }

    public void renderCube(EntityResizableCuboid cube) {
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

        double sizeX = cube.xSize;
        double sizeY = cube.ySize;
        double sizeZ = cube.zSize;

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

    /** Note that this method DOES take into account its position. But not its rotation. (Open an issue on github if you
     * need rotation, and a second method will be made that does all the trig required) */
    public void renderCubeStatic(List<BakedQuad> quads, EntityResizableCuboid cuboid) {
        TextureAtlasSprite[] sprites = cuboid.textures;
        if (sprites == null) {
            sprites = new TextureAtlasSprite[6];
            for (int i = 0; i < 6; i++) {
                sprites[i] = cuboid.texture;
            }
        }

        int[] flips = cuboid.textureFlips;
        if (flips == null) {
            flips = new int[6];
        }

        double textureStartX = cuboid.textureStartX / 16D;
        double textureStartY = cuboid.textureStartY / 16D;
        double textureStartZ = cuboid.textureStartZ / 16D;

        double textureSizeX = cuboid.textureSizeX / 16D;
        double textureSizeY = cuboid.textureSizeY / 16D;
        double textureSizeZ = cuboid.textureSizeZ / 16D;

        double textureEndX = textureSizeX + textureStartX;
        double textureEndY = textureSizeY + textureStartY;
        double textureEndZ = textureSizeZ + textureStartZ;

        double textureOffsetX = cuboid.textureOffsetX / 16D;
        double textureOffsetY = cuboid.textureOffsetY / 16D;
        double textureOffsetZ = cuboid.textureOffsetZ / 16D;

        double sizeX = cuboid.xSize;
        double sizeY = cuboid.ySize;
        double sizeZ = cuboid.zSize;

        if (sprites[0] != null) {
            // Down
            float[] uv = getUVArray(sprites[0], flips[0], textureStartX, textureEndX, textureStartZ, textureEndZ);
            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                ri = ri.offset(cuboid, Axis.Y);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.DOWN);
            }
        }

        if (sprites[1] != null) {
            // Up
            float[] uv = getUVArray(sprites[1], flips[1], textureStartX, textureEndX, textureStartZ, textureEndZ);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                ri = ri.offset(cuboid, Axis.Y);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], sizeY + cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], sizeY + cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], sizeY + cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], sizeY + cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.UP);
            }
        }

        if (sprites[2] != null) {
            // North (-Z)
            float[] uv = getUVArray(sprites[2], flips[2], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.Z);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MIN], cuboid.posZ, -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MAX], cuboid.posZ, -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MAX], cuboid.posZ, -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MIN], cuboid.posZ, -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.NORTH);
            }
        }

        if (sprites[3] != null) {
            // South (+Z)
            float[] uv = getUVArray(sprites[3], flips[3], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.Z);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MIN], cuboid.posZ + sizeZ, -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MAX], cuboid.posZ + sizeZ, -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MAX], cuboid.posZ + sizeZ, -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MIN], cuboid.posZ + sizeZ, -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.SOUTH);
            }
        }

        if (sprites[4] != null) {
            // West (-X)
            float[] uv = getUVArray(sprites[4], flips[4], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.X);
                double[][] arr = new double[4][];
                arr[0] = new double[] { cuboid.posX, ri.xyz[V_MIN], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { cuboid.posX, ri.xyz[V_MAX], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { cuboid.posX, ri.xyz[V_MAX], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { cuboid.posX, ri.xyz[V_MIN], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.WEST);
            }
        }

        if (sprites[5] != null) {
            // East (+X)
            float[] uv = getUVArray(sprites[5], flips[5], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.X);
                double[][] arr = new double[4][];
                arr[0] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MIN], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MAX], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MAX], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MIN], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.EAST);
            }
        }
    }

    private void convertToDoubleQuads(List<BakedQuad> quads, double[][] points, EnumFacing face) {
        BakedQuad quad = convertToQuad(points, face);
        quads.add(quad);

        double[][] otherPoints = new double[][] { points[3], points[2], points[1], points[0] };
        quad = convertToQuad(otherPoints, face);
        quads.add(quad);
    }

    private BakedQuad convertToQuad(double[][] points, EnumFacing face) {
        int[] list = new int[points.length * points[0].length];
        for (int i = 0; i < points.length; i++) {
            double[] arr = points[i];
            for (int j = 0; j < arr.length; j++) {
                double d = arr[j];
                int used = 0;
                if (j == 3 || j == 6) {// Shade or unused
                    used = (int) d;
                } else {
                    used = Float.floatToRawIntBits((float) d);
                }
                list[i * arr.length + j] = used;
            }
        }
        return new BakedQuad(list, 0, face);
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
            float[] uvtu = Arrays.copyOf(uv, 4);
            double addU = textureSizeU;
            boolean lowerU = false;

            // If there is an offset then make sure the texture positions are changed properly
            if (firstU && textureOffsetU != 0) {
                uvtu[U_MIN] = uvtu[U_MIN] + (uvtu[U_MAX] - uvtu[U_MIN]) * (float) textureOffsetU;
                addU -= textureOffsetU;
                // addU = 1 - textureOffsetU;
                lowerU = true;
            }

            // If the size of the texture is greater than the cuboid goes on for then make sure the texture
            // positions are lowered
            if (u + addU > sizeU) {
                addU = sizeU - u;
                if (firstU && textureOffsetU != 0) {
                    uvtu[U_MAX] = uvtu[U_MIN] + (uvtu[U_MAX] - uvtu[U_MIN]) * (float) (addU / (textureSizeU - textureOffsetU));
                } else {
                    uvtu[U_MAX] = uvtu[U_MIN] + (uvtu[U_MAX] - uvtu[U_MIN]) * (float) (addU / textureSizeU);
                }
            }
            firstU = false;
            boolean firstV = true;
            for (double v = 0; v < sizeV; v += textureSizeV) {
                float[] uvtv = Arrays.copyOf(uvtu, 4);

                double addV = textureSizeV;

                boolean lowerV = false;

                if (firstV && textureOffsetV != 0) {
                    uvtv[V_MIN] = uvtv[V_MIN] + (uvtv[V_MAX] - uvtv[V_MIN]) * (float) textureOffsetV;
                    addV -= textureOffsetV;
                    lowerV = true;
                }
                if (v + addV > sizeV) {
                    addV = sizeV - v;
                    if (firstV && textureOffsetV != 0) {
                        uvtv[V_MAX] = uvtv[V_MIN] + (uvtv[V_MAX] - uvtv[V_MIN]) * (float) (addV / (textureSizeV - textureOffsetV));
                    } else {
                        uvtv[V_MAX] = uvtv[V_MIN] + (uvtv[V_MAX] - uvtv[V_MIN]) * (float) (addV / textureSizeV);
                    }
                }

                double[] xyz = new double[4];
                xyz[U_MIN] = u;
                xyz[U_MAX] = u + addU;
                xyz[V_MIN] = v;
                xyz[V_MAX] = v + addV;
                infos.add(new RenderInfo(uvtv, xyz));

                if (lowerV) {
                    v -= textureOffsetV;
                }
                firstV = false;
            }
            // If we lowered the U because the cuboid started on an offset, reset it back to what was actually
            // rendered, not what the for loop assumes
            if (lowerU) {
                u -= textureOffsetU;
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

        public RenderInfo offset(Entity ent, Axis axis) {
            switch (axis) {
                case X: {
                    return new RenderInfo(uv, new double[] { xyz[0] + ent.posZ, xyz[1] + ent.posZ, xyz[2] + ent.posY, xyz[3] + ent.posY });
                }
                case Y: {
                    return new RenderInfo(uv, new double[] { xyz[0] + ent.posX, xyz[1] + ent.posX, xyz[2] + ent.posZ, xyz[3] + ent.posZ });
                }
                case Z: {
                    return new RenderInfo(uv, new double[] { xyz[0] + ent.posX, xyz[1] + ent.posX, xyz[2] + ent.posY, xyz[3] + ent.posY });
                }
            }
            return new RenderInfo(uv, xyz);
        }
    }
}
