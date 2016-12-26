package buildcraft.lib.client.render.fluid;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.VecUtil;

/** Can render 3D fluid cuboid's, up to 1x1x1 in size. Note that they *must* be contained within the 1x1x1 block space -
 * you can't use this to render off large multiblocks.
 * 
 * Not thread safe -- this uses static variables so you should only call this from the main client thread. */
// TODO: thread safety (per thread context?)
public class FluidRenderer {

    private static final EnumMap<FluidSpriteType, Map<Fluid, TextureAtlasSprite>> fluidSprites = new EnumMap<>(FluidSpriteType.class);
    public static final MutableVertex vertex = new MutableVertex();
    private static final boolean[] DEFAULT_FACES = { true, true, true, true, true, true };

    // Cached fields that prevent lots of arguments on most methods
    private static VertexBuffer vb;
    private static TextureAtlasSprite sprite;
    private static TexMap texmap;
    private static boolean invertU, invertV;
    private static double xTexDiff, yTexDiff, zTexDiff;

    static {
        // TODO: allow the caller to change the light level
        vertex.lighti(0xF, 0xF);
        for (FluidSpriteType type : FluidSpriteType.values()) {
            fluidSprites.put(type, new HashMap<>());
        }
    }

    public static void onTextureStitchPre(TextureMap map) {
        for (FluidSpriteType type : FluidSpriteType.values()) {
            fluidSprites.get(type).clear();
        }
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            TextureAtlasSprite stillSprite = map.registerSprite(fluid.getStill());
            fluidSprites.get(FluidSpriteType.STILL).put(fluid, stillSprite);
            fluidSprites.get(FluidSpriteType.FLOWING).put(fluid, map.registerSprite(fluid.getFlowing()));
            SpriteFluidFrozen sproteFrozen = new SpriteFluidFrozen(stillSprite);
            map.setTextureEntry(sproteFrozen);
            fluidSprites.get(FluidSpriteType.FROZEN).put(fluid, sproteFrozen);
        }
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     * 
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param tank The fluid tank that should be rendered.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param vbIn The {@link VertexBuffer} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     *            faces will be rendered. The indexes are determined by what {@link EnumFacing#ordinal()} returns.
     * 
     * @see #renderFluid(FluidSpriteType, FluidStack, double, double, Vec3d, Vec3d, VertexBuffer, boolean[]) */
    public static void renderFluid(FluidSpriteType type, IFluidTank tank, Vec3d min, Vec3d max, VertexBuffer vbIn, boolean[] sideRender) {
        renderFluid(type, tank.getFluid(), tank.getCapacity(), min, max, vbIn, sideRender);
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     * 
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param fluid The stack that represents the fluid to render
     * @param cap The maximum amount of fluid that could be in the stack. Usually the capacity of the tank.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param vbIn The {@link VertexBuffer} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     *            faces will be rendered. The indexes are determined by what {@link EnumFacing#ordinal()} returns. */
    public static void renderFluid(FluidSpriteType type, FluidStack fluid, int cap, Vec3d min, Vec3d max, VertexBuffer vbIn, boolean[] sideRender) {
        renderFluid(type, fluid, fluid == null ? 0 : fluid.amount, cap, min, max, vbIn, sideRender);
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     * 
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param fluid The stack that represents the fluid to render. Note that the amount from the stack is NOT used.
     * @param amount The actual amount of fluid in the stack. Is a "double" rather than an "int" as then you can
     *            interpolate between frames.
     * @param cap The maximum amount of fluid that could be in the stack. Usually the capacity of the tank.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param vbIn The {@link VertexBuffer} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     *            faces will be rendered. The indexes are determined by what {@link EnumFacing#ordinal()} returns. */
    public static void renderFluid(FluidSpriteType type, FluidStack fluid, double amount, double cap, Vec3d min, Vec3d max, VertexBuffer vbIn, boolean[] sideRender) {
        if (fluid == null || fluid.getFluid() == null || amount <= 0) {
            return;
        }
        if (sideRender == null) {
            sideRender = DEFAULT_FACES;
        }

        double height = MathHelper.clamp(amount / cap, 0, 1);
        final Vec3d realMin, realMax;
        if (fluid.getFluid().isGaseous(fluid)) {
            realMin = VecUtil.replaceValue(min, Axis.Y, MathUtil.interp(1 - height, min.yCoord, max.yCoord));
            realMax = max;
        } else {
            realMin = min;
            realMax = VecUtil.replaceValue(max, Axis.Y, MathUtil.interp(height, min.yCoord, max.yCoord));
        }

        vb = vbIn;

        if (type == null) {
            type = FluidSpriteType.STILL;
        }
        sprite = fluidSprites.get(type).get(fluid.getFluid());
        if (sprite == null) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        final double xs = realMin.xCoord;
        final double ys = realMin.yCoord;
        final double zs = realMin.zCoord;

        final double xb = realMax.xCoord;
        final double yb = realMax.yCoord;
        final double zb = realMax.zCoord;

        if (type == FluidSpriteType.FROZEN) {
            if (min.xCoord > 1) {
                xTexDiff = Math.floor(min.xCoord);
            } else if (min.xCoord < 0) {
                xTexDiff = Math.floor(min.xCoord);
            } else {
                xTexDiff = 0;
            }
            if (min.yCoord > 1) {
                yTexDiff = Math.floor(min.yCoord);
            } else if (min.yCoord < 0) {
                yTexDiff = Math.floor(min.yCoord);
            } else {
                yTexDiff = 0;
            }
            if (min.zCoord > 1) {
                zTexDiff = Math.floor(min.zCoord);
            } else if (min.zCoord < 0) {
                zTexDiff = Math.floor(min.zCoord);
            } else {
                zTexDiff = 0;
            }
        } else {
            xTexDiff = 0;
            yTexDiff = 0;
            zTexDiff = 0;
        }

        vertex.colouri(RenderUtil.swapARGBforABGR(fluid.getFluid().getColor(fluid)));

        texmap = TexMap.XZ;
        // TODO: Enable/disable inversion for the correct faces
        invertU = false;
        invertV = false;
        if (sideRender[EnumFacing.UP.ordinal()]) {
            vertex(xs, yb, zb);
            vertex(xb, yb, zb);
            vertex(xb, yb, zs);
            vertex(xs, yb, zs);
        }

        if (sideRender[EnumFacing.DOWN.ordinal()]) {
            vertex(xs, ys, zs);
            vertex(xb, ys, zs);
            vertex(xb, ys, zb);
            vertex(xs, ys, zb);
        }

        texmap = TexMap.ZY;
        if (sideRender[EnumFacing.WEST.ordinal()]) {
            vertex(xs, ys, zs);
            vertex(xs, ys, zb);
            vertex(xs, yb, zb);
            vertex(xs, yb, zs);
        }

        if (sideRender[EnumFacing.EAST.ordinal()]) {
            vertex(xb, yb, zs);
            vertex(xb, yb, zb);
            vertex(xb, ys, zb);
            vertex(xb, ys, zs);
        }

        texmap = TexMap.XY;
        if (sideRender[EnumFacing.NORTH.ordinal()]) {
            vertex(xs, yb, zs);
            vertex(xb, yb, zs);
            vertex(xb, ys, zs);
            vertex(xs, ys, zs);
        }

        if (sideRender[EnumFacing.SOUTH.ordinal()]) {
            vertex(xs, ys, zb);
            vertex(xb, ys, zb);
            vertex(xb, yb, zb);
            vertex(xs, yb, zb);
        }

        sprite = null;
        texmap = null;
        vb = null;
    }

    /** Helper function to add a vertex. */
    private static void vertex(double x, double y, double z) {
        vertex.positiond(x, y, z);
        texmap.apply(x - xTexDiff, y - yTexDiff, z - zTexDiff);
        vertex.render(vb);
    }

    /** Fills up the given region with the fluids texture, repeated. Ignores the value of {@link FluidStack#amount}. Use
     * {@link GuiUtil}'s fluid drawing methods in preference to this. */
    public static void drawFluidForGui(FluidStack fluid, int startX, int startY, int endX, int endY) {

        sprite = FluidRenderer.fluidSprites.get(FluidSpriteType.STILL).get(fluid.getFluid());
        if (sprite == null) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderUtil.setGLColorFromInt(fluid.getFluid().getColor(fluid));

        Tessellator tess = Tessellator.getInstance();
        vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // draw all the full sprites

        int diffX = endX - startX;
        int diffY = endY - startY;

        int stepX = diffX > 0 ? 16 : -16;
        int stepY = diffY > 0 ? 16 : -16;

        int loopEndX = startX + 16 * (diffX / 16);
        int loopEndY = startY + 16 * (diffY / 16);

        // This is broken atm!
        for (int x = startX; x != loopEndX; x += stepX) {
            for (int y = startY; y != loopEndY; y += stepY) {
                guiVertex(x, y, 0, 0);
                guiVertex(x + stepX, y, 16, 0);
                guiVertex(x + stepX, y + stepY, 16, 16);
                guiVertex(x, y + stepY, 0, 16);
            }
        }

        if (diffX % 16 != 0) {
            int additionalWidth = diffX % 16;
            int x = endX - additionalWidth;
            int xTex = additionalWidth < 0 ? -additionalWidth : additionalWidth;
            for (int y = startY; y != loopEndY; y += stepY) {
                guiVertex(x, y, 0, 0);
                guiVertex(endX, y, xTex, 0);
                guiVertex(endX, y + stepY, xTex, 16);
                guiVertex(x, y + stepY, 0, 16);
            }
        }

        if (diffY % 16 != 0) {
            int additionalHeight = diffY % 16;
            int y = endY - additionalHeight;
            int yTex = additionalHeight < 0 ? -additionalHeight : additionalHeight;
            for (int x = startX; x != loopEndX; x += stepX) {
                guiVertex(x, y, 0, 0);
                guiVertex(x + stepX, y, 16, 0);
                guiVertex(x + stepX, endY, 16, yTex);
                guiVertex(x, endY, 0, yTex);
            }
        }
        tess.draw();
        GlStateManager.color(1, 1, 1);
        sprite = null;
        vb = null;
    }

    private static void guiVertex(int x, int y, int u, int v) {
        float ru = sprite.getInterpolatedU(u);
        float rv = sprite.getInterpolatedV(v);
        vb.pos(x, y, 0);
        vb.tex(ru, rv);
        vb.endVertex();
    }

    /** Used to keep track of what position maps to what texture co-ord.
     * <p>
     * For example XY maps X to U and Y to V, and ignores Z */
    private enum TexMap {
        XY(true, true),
        XZ(true, false),
        ZY(false, true);

        /** If true, then X maps to U. Otherwise Z maps to U. */
        private final boolean ux;
        /** If true, then Y maps to V. Otherwise Z maps to V. */
        private final boolean vy;

        private TexMap(boolean ux, boolean vy) {
            this.ux = ux;
            this.vy = vy;
        }

        /** Changes the vertex's texture co-ord to be the same as the position, for that face.
         * 
         * (Uses {@link #ux} and {@link #vy} to determine how they are mapped). */
        private void apply(double x, double y, double z) {
            double realu = ux ? x : z;
            double realv = vy ? y : z;
            if (invertU) {
                realu = 1 - realu;
            }
            if (invertV) {
                realv = 1 - realv;
            }
            vertex.texf(sprite.getInterpolatedU(realu * 16), sprite.getInterpolatedV(realv * 16));
        }
    }
}
