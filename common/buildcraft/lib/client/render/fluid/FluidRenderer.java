package buildcraft.lib.client.render.fluid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import buildcraft.core.lib.utils.MathUtils;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;

/** Can render 3D fluid cuboid's, up to 1x1x1 in size. Note that they *must* be contained within the 1x1x1 block space -
 * you can't use this to render off large multiblocks.
 * 
 * Not thread safe -- this uses static variables so you should only call this from the main client thread. */
// TODO: thread safety (per thread context?)
public class FluidRenderer {
    private static final Map<Fluid, TextureAtlasSprite> fluidSprites = new ConcurrentHashMap<>();
    private static final MutableVertex vertex = new MutableVertex();
    private static final boolean[] DEFAULT_FACES = { true, true, true, true, true, true };

    // Cached fields that prevent lots of arguments on most methods
    private static VertexBuffer vb;
    private static TextureAtlasSprite sprite;
    private static TexMap texmap;
    private static boolean invertU, invertV;

    static {
        // TODO: allow the caller to change the light level
        vertex.lighti(0xF, 0xF);
    }

    public static void onTextureStitchPre(TextureMap map) {
        fluidSprites.clear();
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            fluidSprites.put(fluid, map.registerSprite(fluid.getStill()));
            // TODO: both other types (FLOWING and FROZEN)
        }
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any 0->1 boundary (so the cube
     * must be contained within a block).
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

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any 0->1 boundary (so the cube
     * must be contained within a block).
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

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any 0->1 boundary (so the cube
     * must be contained within a block).
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

        double height = MathUtils.clamp(amount / cap, 0, 1);
        final Vec3d realMin, realMax;
        if (fluid.getFluid().isGaseous(fluid)) {
            realMin = VecUtil.replaceValue(min, Axis.Y, MathUtil.interp(1 - height, min.yCoord, max.yCoord));
            realMax = max;
        } else {
            realMin = min;
            realMax = VecUtil.replaceValue(max, Axis.Y, MathUtil.interp(height, min.yCoord, max.yCoord));
        }

        vb = vbIn;

        // TODO: use type to determine the sprite
        sprite = fluidSprites.get(fluid.getFluid());
        if (sprite == null) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        final double xs = realMin.xCoord;
        final double ys = realMin.yCoord;
        final double zs = realMin.zCoord;

        final double xb = realMax.xCoord;
        final double yb = realMax.yCoord;
        final double zb = realMax.zCoord;

        vertex.colouri(fluid.getFluid().getColor(fluid));

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
        texmap.apply(x, y, z);
        vertex.render(vb);
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
            // TODO: this doesn't work when the coord is exactly 1!
            x = (x % 1 + 1) % 1;
            y = (y % 1 + 1) % 1;
            z = (z % 1 + 1) % 1;
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
