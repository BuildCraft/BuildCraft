/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.fluid;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Can render 3D fluid cuboid's, up to 1x1x1 in size. Note that they *must* be contained within the 1x1x1 block space -
 * you can't use this to render off large multiblocks. Not thread safe -- this uses static variables so you should only
 * call this from the main client thread. */
// TODO: thread safety (per thread context?)
// Perhaps move this into IModelRenderer? And that way we get the buffer, force shaders to cope with fluids (?!), etc
@OnlyIn(Dist.CLIENT)
public class FluidRenderer {

    private static final EnumMap<FluidSpriteType, Map<String, TextureAtlasSprite>> fluidSprites =
            new EnumMap<>(FluidSpriteType.class);
    public static final MutableVertex vertex = new MutableVertex();
    private static final boolean[] DEFAULT_FACES = { true, true, true, true, true, true };

    // Cached fields that prevent lots of arguments on most methods
//    private static BufferBuilder bb;
    private static IVertexBuilder bb;
    private static MatrixStack.Entry pose;
    private static TextureAtlasSprite sprite;
    private static TexMap texmap;
    private static boolean invertU, invertV;
    private static double xTexDiff, yTexDiff, zTexDiff;

    static {
        // TODO: allow the caller to change the light level
        vertex.lighti((byte) (0xF >> 4), (byte) (0xF >> 4));
        for (FluidSpriteType type : FluidSpriteType.values()) {
            fluidSprites.put(type, new HashMap<>());
        }
    }

    // TODO Calen frozen
//    public static void onTextureStitchPre(AtlasTexture map)
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        for (FluidSpriteType type : FluidSpriteType.values()) {
            fluidSprites.get(type).clear();
        }
        Map<ResourceLocation, SpriteFluidFrozen> spritesStitched = new HashMap<>();

        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            // Calen
            if (fluid.getClass() == EmptyFluid.class) {
                continue;
            }
            // BC 1.12.2
            ResourceLocation still = fluid.getAttributes().getStillTexture();
            ResourceLocation flowing = fluid.getAttributes().getFlowingTexture();
            if (still == null || flowing == null) {
                // Calen: for uncompleted fluid, continue
//                throw new IllegalStateException("Encountered a fluid with a null still sprite! (" + fluid.getRegistryName().toString()
//                        + " - " + ForgeRegistries.FLUIDS.getKey(fluid) + ")");
                continue;
            }
            if (spritesStitched.containsKey(still)) {
                // TODO Calen frozen?
//                fluidSprites.get(FluidSpriteType.FROZEN).put(fluid.getRegistryName().toString(), spritesStitched.get(still));
//                event.addSprite(frozen);
            } else {
                // TODO Calen frozen
//                try
//                {
//                    SpriteFluidFrozen spriteFrozen = new SpriteFluidFrozen(still);
//                    spritesStitched.put(still, spriteFrozen);
//                }
//                catch (IOException e)
//                {
//                    BCLog.logger.error("[lib.fluid.renderder] Failed to create spriteFrozen", e);
//                }
                // TODO Calen frozen? how to create TextureAtlasSprite new instance?
//                if (!map.setTextureEntry(spriteFrozen))
//                {
//                    throw new IllegalStateException("Failed to set the frozen variant of " + still + "!");
//                }
//                fluidSprites.get(FluidSpriteType.FROZEN).put(fluid.getRegistryName().toString(), spriteFrozen);
            }
            // Note: this must be called with EventPriority.LOW so that we don't overwrite other custom sprites.
//            fluidSprites.get(FluidSpriteType.STILL).put(fluid.getRegistryName().toString(), map.getSprite(still));
//            fluidSprites.get(FluidSpriteType.FLOWING).put(fluid.getRegistryName().toString(), map.getSprite(flowing));
            // Calen: not need to add here, they're added by model loading
            // if a fluid has no textuer (uncompleted fluid) -> IllegalStateException: Encountered a fluid with a null still sprite!
//            event.addSprite(still);
//            event.addSprite(flowing);
        }
    }

    // Calen: part of onTextureStitchPre in 1.12.2
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        // ensure LOCATION_BLOCKS
        // or will get wrong texture
        AtlasTexture map = event.getMap();
        if (map.location().equals(AtlasTexture.LOCATION_BLOCKS)) {
            // Calen: BC
            for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                // Calen
                if (fluid.getClass() == EmptyFluid.class) {
                    continue;
                }
                ResourceLocation still = fluid.getAttributes().getStillTexture();
                ResourceLocation flowing = fluid.getAttributes().getFlowingTexture();
                if (still == null || flowing == null) {
                    // Calen: for uncompleted fluid, continue
                    BCLog.logger.warn("[lib.fluid.renderder] Found fluid [" + fluid.getRegistryName() + "] has no still or flow textuer ResourceLocation, unable to get sprite.");
                    continue;
                }
                fluidSprites.get(FluidSpriteType.STILL).put(fluid.getRegistryName().toString(), map.getSprite(still));
                fluidSprites.get(FluidSpriteType.FLOWING).put(fluid.getRegistryName().toString(), map.getSprite(flowing));
            }
        }
    }

    /** Renders a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     *
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param tank The fluid tank that should be rendered.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param bbIn The {@link BufferBuilder} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     * faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns.
     * @see #renderFluid(FluidSpriteType, FluidStack, double, double, Vector3d, Vector3d, MatrixStack.Entry, IVertexBuilder, boolean[]) */
    public static void renderFluid(FluidSpriteType type, IFluidTank tank, Vector3d min, Vector3d max, MatrixStack.Entry pose, IVertexBuilder bbIn,
                                   boolean[] sideRender) {
        renderFluid(type, tank.getFluid(), tank.getCapacity(), min, max, pose, bbIn, sideRender);
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     *
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param fluid The stack that represents the fluid to render
     * @param cap The maximum amount of fluid that could be in the stack. Usually the capacity of the tank.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param bbIn The {@link BufferBuilder} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     * faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns. */
    public static void renderFluid(
            FluidSpriteType type,
            FluidStack fluid,
            int cap,
            Vector3d min,
            Vector3d max,
//            BufferBuilder bbIn,
            MatrixStack.Entry pose,
            IVertexBuilder bbIn,
            boolean[] sideRender
    ) {
        renderFluid(type, fluid, fluid == null ? 0 : fluid.getAmount(), cap, min, max, pose, bbIn, sideRender);
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     *
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param fluid The stack that represents the fluid to render. Note that the amount from the stack is NOT used.
     * @param amount The actual amount of fluid in the stack. Is a "double" rather than an "int" as then you can
     * interpolate between frames.
     * @param cap The maximum amount of fluid that could be in the stack. Usually the capacity of the tank.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param poseIn The {@link MatrixStack.Entry}
     * @param bbIn The {@link IVertexBuilder} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     * faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns. */
    public static void renderFluid(
            FluidSpriteType type,
            FluidStack fluid,
            double amount,
            double cap,
            Vector3d min,
            Vector3d max,
            MatrixStack.Entry poseIn,
            IVertexBuilder bbIn,
            boolean[] sideRender
    ) {
        if (fluid == null || fluid.getRawFluid() == null || amount <= 0) {
            return;
        }
        IProfiler prof = Minecraft.getInstance().getProfiler();
        prof.push("fluid");
        if (sideRender == null) {
            sideRender = DEFAULT_FACES;
        }

//        double height = MathHelper.clamp(amount / cap, 0, 1);
        double height = MathHelper.clamp(amount / cap, 0, 1);
        final Vector3d realMin, realMax;
        if (fluid.getRawFluid().getAttributes().isGaseous(fluid)) {
            realMin = VecUtil.replaceValue(min, Axis.Y, MathUtil.interp(1 - height, min.y, max.y));
            realMax = max;
        } else {
            realMin = min;
            realMax = VecUtil.replaceValue(max, Axis.Y, MathUtil.interp(height, min.y, max.y));
        }

        FluidRenderer.bb = bbIn;
        FluidRenderer.pose = poseIn;

        if (type == null) {
            type = FluidSpriteType.STILL;
        }
        sprite = getFluidSprite(type, fluid);

        final double xs = realMin.x;
        final double ys = realMin.y;
        final double zs = realMin.z;

        final double xb = realMax.x;
        final double yb = realMax.y;
        final double zb = realMax.z;

        // TODO Calen FROZEN AtlasTexture
        if (type == FluidSpriteType.FROZEN) {
            if (min.x > 1) {
                xTexDiff = Math.floor(min.x);
            } else if (min.x < 0) {
                xTexDiff = Math.floor(min.x);
            } else {
                xTexDiff = 0;
            }
            if (min.y > 1) {
                yTexDiff = Math.floor(min.y);
            } else if (min.y < 0) {
                yTexDiff = Math.floor(min.y);
            } else {
                yTexDiff = 0;
            }
            if (min.z > 1) {
                zTexDiff = Math.floor(min.z);
            } else if (min.z < 0) {
                zTexDiff = Math.floor(min.z);
            } else {
                zTexDiff = 0;
            }
        } else {
            xTexDiff = 0;
            yTexDiff = 0;
            zTexDiff = 0;
        }

        vertex.colouri(RenderUtil.swapARGBforABGR(fluid.getRawFluid().getAttributes().getColor(fluid)));

        texmap = TexMap.XZ;
        // TODO: Enable/disable inversion for the correct faces
        invertU = false;
        invertV = false;
        if (sideRender[Direction.UP.ordinal()]) {
            vertex(xs, yb, zb);
            vertex(xb, yb, zb);
            vertex(xb, yb, zs);
            vertex(xs, yb, zs);
        }

        if (sideRender[Direction.DOWN.ordinal()]) {
            vertex(xs, ys, zs);
            vertex(xb, ys, zs);
            vertex(xb, ys, zb);
            vertex(xs, ys, zb);
        }

        texmap = TexMap.ZY;
        if (sideRender[Direction.WEST.ordinal()]) {
            vertex(xs, ys, zs);
            vertex(xs, ys, zb);
            vertex(xs, yb, zb);
            vertex(xs, yb, zs);
        }

        if (sideRender[Direction.EAST.ordinal()]) {
            vertex(xb, yb, zs);
            vertex(xb, yb, zb);
            vertex(xb, ys, zb);
            vertex(xb, ys, zs);
        }

        texmap = TexMap.XY;
        if (sideRender[Direction.NORTH.ordinal()]) {
            vertex(xs, yb, zs);
            vertex(xb, yb, zs);
            vertex(xb, ys, zs);
            vertex(xs, ys, zs);
        }

        if (sideRender[Direction.SOUTH.ordinal()]) {
            vertex(xs, ys, zb);
            vertex(xb, ys, zb);
            vertex(xb, yb, zb);
            vertex(xs, yb, zb);
        }

        sprite = null;
        texmap = null;
        FluidRenderer.bb = null;
        FluidRenderer.pose = null;
        prof.pop();
    }

    public static TextureAtlasSprite getFluidSprite(FluidSpriteType type, FluidStack fluid) {
        return getFluidSprite(type, fluid.getRawFluid());
    }

    public static TextureAtlasSprite getFluidSprite(FluidSpriteType type, Fluid fluid) {
        if (fluid == null) {
            return SpriteUtil.missingSprite();
        }
        TextureAtlasSprite s = fluidSprites.get(type).get(fluid.getRegistryName().toString());
        if (s == null) {
            ResourceLocation spriteLocation = null;
            switch (type) {
                case STILL:
                    spriteLocation = fluid.getAttributes().getStillTexture();
                    break;
                case FLOWING:
                    spriteLocation = fluid.getAttributes().getFlowingTexture();
                    break;
                // TODO Calen FROZEN???
                case FROZEN:
//                    spriteLocation = null;
                    spriteLocation = fluid.getAttributes().getStillTexture();
                    break;
            }
            s = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(spriteLocation);
            if (s != null) {
                fluidSprites.get(type).put(fluid.getRegistryName().toString(), s);
            }
        }
        return s != null ? s : SpriteUtil.missingSprite();
    }

    /** Helper function to add a vertex. */
    private static void vertex(double x, double y, double z) {
        vertex.positiond(x, y, z);
        texmap.apply(x - xTexDiff, y - yTexDiff, z - zTexDiff);
        vertex.renderAsBlock(pose, bb);
    }

    /** Fills up the given region with the fluids texture, repeated. Ignores the value of {@link FluidStack#getAmount()}. Use
     * {@link GuiUtil}'s fluid drawing methods in preference to this. */
    public static void drawFluidForGui(FluidStack fluid, double startX, double startY, double endX, double endY, MatrixStack poseStack) {
        MatrixStack.Entry pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();

        sprite = FluidRenderer.fluidSprites.get(FluidSpriteType.STILL).get(fluid.getRawFluid().getRegistryName().toString());
        if (sprite == null) {
//            sprite = Minecraft.getInstance().getTextureMapBlocks().getMissingSprite();
            sprite = SpriteUtil.missingSprite();
        }
//        Minecraft.getInstance().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        SpriteUtil.bindTexture(AtlasTexture.LOCATION_BLOCKS);
//        RenderUtil.setGLColorFromInt(fluid.getFluid().getColor(fluid));
        RenderUtil.setGLColorFromInt(fluid.getFluid().getAttributes().getColor(fluid));

//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        Tessellator tess = Tessellator.getInstance();
        Tessellator tess = Tessellator.getInstance();
//        bb = tess.getBuffer();
        bb = tess.getBuilder();
//        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        ((BufferBuilder) bb).begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // draw all the full sprites

        double diffX = endX - startX;
        double diffY = endY - startY;

        int stepX = diffX > 0 ? 16 : -16;
        int stepY = diffY > 0 ? 16 : -16;

        int loopCountX = (int) Math.abs(diffX / 16);
        int loopCountY = (int) Math.abs(diffY / 16);

        double x = startX;
        for (int xc = 0; xc < loopCountX; xc++) {
            double y = startY;
            for (int yc = 0; yc < loopCountY; yc++) {
                guiVertex(poseMatrix, x, y, 0, 0);
                guiVertex(poseMatrix, x + stepX, y, 16, 0);
                guiVertex(poseMatrix, x + stepX, y + stepY, 16, 16);
                guiVertex(poseMatrix, x, y + stepY, 0, 16);
                y += stepY;
            }
            x += stepX;
        }

        if (diffX % 16 != 0) {
            double additionalWidth = diffX % 16;
            x = endX - additionalWidth;
            double xTex = Math.abs(additionalWidth);
            double y = startY;
            for (int yc = 0; yc < loopCountY; y++) {
                guiVertex(poseMatrix, x, y, 0, 0);
                guiVertex(poseMatrix, endX, y, xTex, 0);
                guiVertex(poseMatrix, endX, y + stepY, xTex, 16);
                guiVertex(poseMatrix, x, y + stepY, 0, 16);
                y += stepY;
            }
        }

        if (diffY % 16 != 0) {
            double additionalHeight = diffY % 16;
            double y = endY - additionalHeight;
            double yTex = Math.abs(additionalHeight);
            x = startX;
            for (int xc = 0; xc < loopCountX; xc++) {
                guiVertex(poseMatrix, x, y, 0, 0);
                guiVertex(poseMatrix, x + stepX, y, 16, 0);
                guiVertex(poseMatrix, x + stepX, endY, 16, yTex);
                guiVertex(poseMatrix, x, endY, 0, yTex);
                x += stepX;
            }
        }

        if (diffX % 16 != 0 && diffY % 16 != 0) {
            double w = diffX % 16;
            double h = diffY % 16;
            x = endX - w;
            double y = endY - h;
            double tx = w < 0 ? -w : w;
            double ty = h < 0 ? -h : h;
            guiVertex(poseMatrix, x, y, 0, 0);
            guiVertex(poseMatrix, endX, y, tx, 0);
            guiVertex(poseMatrix, endX, endY, tx, ty);
            guiVertex(poseMatrix, x, endY, 0, ty);
        }

//        tess.draw();
        tess.end();
//        GlStateManager.color(1, 1, 1);
        RenderUtil.color(1, 1, 1);
        sprite = null;
        bb = null;
    }

    private static void guiVertex(Matrix4f poseMatrix, double x, double y, double u, double v) {
//        float ru = sprite.getInterpolatedU(u);
        float ru = sprite.getU(u);
//        float rv = sprite.getInterpolatedV(v);
        float rv = sprite.getV(v);
//        poseStack.translate(x, y, 0);
        bb.vertex(poseMatrix, (float) x, (float) y, 0);
//        bb.tex(ru, rv);
        bb.uv(ru, rv);
        bb.endVertex();
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

        TexMap(boolean ux, boolean vy) {
            this.ux = ux;
            this.vy = vy;
        }

        /** Changes the vertex's texture co-ord to be the same as the position, for that face. (Uses {@link #ux} and
         * {@link #vy} to determine how they are mapped). */
        private void apply(double x, double y, double z) {
            double realu = ux ? x : z;
            double realv = vy ? y : z;
            if (invertU) {
                realu = 1 - realu;
            }
            if (invertV) {
                realv = 1 - realv;
            }
//            vertex.texf(sprite.getInterpolatedU(realu * 16), sprite.getInterpolatedV(realv * 16));
            vertex.texf(sprite.getU(realu * 16), sprite.getV(realv * 16));
        }
    }

    public static class TankSize {
        public final Vector3d min;
        public final Vector3d max;

        public TankSize(int sx, int sy, int sz, int ex, int ey, int ez) {
            this(new Vector3d(sx, sy, sz).scale(1 / 16.0), new Vector3d(ex, ey, ez).scale(1 / 16.0));
        }

        public TankSize(Vector3d min, Vector3d max) {
            this.min = min;
            this.max = max;
        }

        public TankSize shrink(double by) {
            return shrink(by, by, by);
        }

        public TankSize shrink(double x, double y, double z) {
            return new TankSize(min.add(x, y, z), max.subtract(x, y, z));
        }

        public TankSize shink(Vector3d by) {
            return shrink(by.x, by.y, by.z);
        }

        public TankSize rotateY() {
            Vector3d _min = rotateY(min);
            Vector3d _max = rotateY(max);
            return new TankSize(VecUtil.min(_min, _max), VecUtil.max(_min, _max));
        }

        private static Vector3d rotateY(Vector3d vec) {
            return new Vector3d(//
                    1 - vec.z, //
                    vec.y, //
                    vec.x//
            );
        }
    }
}
