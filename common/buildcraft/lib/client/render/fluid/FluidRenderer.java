/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.fluid;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCLib;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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
    private static VertexConsumer bb;
    private static PoseStack.Pose pose;
    private static TextureAtlasSprite sprite;
    private static TexMap texmap;
    private static boolean invertU, invertV;
    private static double xTexDiff, yTexDiff, zTexDiff;

    // Calen 1.20.1
    public static final ResourceLocation TEXTURE_ATLAS_FROZEN_LOCATION = new ResourceLocation(BCLib.MODID, "textures/atlas/frozen.png");
    public static final TextureAtlas FROZEN_ATLAS = new TextureAtlas(TEXTURE_ATLAS_FROZEN_LOCATION);
    public static final RenderType FROZEN_FLUID_RENDER_TYPE_TRANSLUCENT = RenderType.create(
            "buildcraft_frozen_fluid_translucent",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.BIG_BUFFER_SIZE,
            /*affectsCrumbling*/ true,
            /*sortOnUpload*/ false,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderUtil.BCCustomizableTextureState(TEXTURE_ATLAS_FROZEN_LOCATION, () -> false, () -> false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setOutputState(RenderStateShard.MAIN_TARGET) // if TRANSLUCENT_TARGET, the frozen fluid will not be rendered when Minecraft.instance.options.graphicsMode().get() == GraphicsStatus.FABULOUS
                    .createCompositeState(true)
    );
    public static final RenderType COMMON_FLUID_RENDER_TYPE_TRANSLUCENT = RenderType.create(
            "buildcraft_common_fluid_translucent",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.BIG_BUFFER_SIZE,
            /*affectsCrumbling*/ true,
            /*sortOnUpload*/ false,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderUtil.BCCustomizableTextureState(TextureAtlas.LOCATION_BLOCKS, () -> false, () -> false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setOutputState(RenderStateShard.MAIN_TARGET) // if TRANSLUCENT_TARGET, the frozen fluid will not be rendered when Minecraft.instance.options.graphicsMode().get() == GraphicsStatus.FABULOUS
                    .createCompositeState(true)
    );

    static {
        // TODO: allow the caller to change the light level
        vertex.lighti((byte) (0xF >> 4), (byte) (0xF >> 4));
        for (FluidSpriteType type : FluidSpriteType.values()) {
            fluidSprites.put(type, new HashMap<>());
        }

        Minecraft.getInstance().textureManager.register(FROZEN_ATLAS.location(), FROZEN_ATLAS);
    }

//    public static void onTextureStitchPre(TextureMap map) {
//        for (FluidSpriteType type : FluidSpriteType.values()) {
//            fluidSprites.get(type).clear();
//        }
//        Map<ResourceLocation, SpriteFluidFrozen> spritesStitched = new HashMap<>();
//        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
//            ResourceLocation still = fluid.getStill();
//            ResourceLocation flowing = fluid.getFlowing();
//            if (still == null || flowing == null) {
//                throw new IllegalStateException("Encountered a fluid with a null still sprite! (" + fluid.getName()
//                        + " - " + FluidRegistry.getDefaultFluidName(fluid) + ")");
//            }
//            if (spritesStitched.containsKey(still)) {
//                fluidSprites.get(FluidSpriteType.FROZEN).put(fluid.getName(), spritesStitched.get(still));
//            } else {
//                SpriteFluidFrozen spriteFrozen = new SpriteFluidFrozen(still);
//                spritesStitched.put(still, spriteFrozen);
//                if (!map.setTextureEntry(spriteFrozen)) {
//                    throw new IllegalStateException("Failed to set the frozen variant of " + still + "!");
//                }
//                fluidSprites.get(FluidSpriteType.FROZEN).put(fluid.getName(), spriteFrozen);
//            }
//            // Note: this must be called with EventPriority.LOW so that we don't overwrite other custom sprites.
//            fluidSprites.get(FluidSpriteType.STILL).put(fluid.getName(), map.registerSprite(still));
//            fluidSprites.get(FluidSpriteType.FLOWING).put(fluid.getName(), map.registerSprite(flowing));
//        }

    // Calen: part of onTextureStitchPre in 1.12.2
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        // ensure LOCATION_BLOCKS
        // or we will get wrong texture
        TextureAtlas map = event.getAtlas();
        if (map.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            SpriteLoader spriteLoader = SpriteLoader.create(FROZEN_ATLAS);
            List<SpriteContents> spriteContentsList = Lists.newArrayList();
            // Calen: BC 1.12.2
            for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                // Calen
                if (fluid.getClass() == EmptyFluid.class) {
                    continue;
                }
                ResourceLocation still = FluidUtilBC.getStillTexture(fluid);
                ResourceLocation flowing = FluidUtilBC.getFlowingTexture(fluid);
                ResourceLocation registryName = FluidUtilBC.getRegistryName(fluid);
                if (still == null || flowing == null) {
                    // Calen: for uncompleted fluid, continue
                    BCLog.logger.warn("[lib.fluid.renderder] Found fluid [" + registryName + "] has no still or flow textuer ResourceLocation, unable to get sprite.");
                    continue;
                }
                TextureAtlasSprite stillSprite = map.getSprite(still);
                fluidSprites.get(FluidSpriteType.STILL).put(registryName.toString(), stillSprite);
                fluidSprites.get(FluidSpriteType.FLOWING).put(registryName.toString(), map.getSprite(flowing));
                spriteContentsList.add(createFrozenSprite(registryName, stillSprite));
            }
            SpriteLoader.Preparations preparations = spriteLoader.stitch(spriteContentsList, Minecraft.getInstance().options.mipmapLevels().get(), Runnable::run);
            FROZEN_ATLAS.upload(preparations);
            FROZEN_ATLAS.getTextureLocations().forEach(location -> {
                SpriteFluidFrozen spriteFluidFrozen = (SpriteFluidFrozen) FROZEN_ATLAS.getSprite(location);
                fluidSprites.get(FluidSpriteType.FROZEN).put(spriteFluidFrozen.srcRegistryName.toString(), spriteFluidFrozen);
            });
        }
    }

    private static SpriteContents createFrozenSprite(ResourceLocation registryName, TextureAtlasSprite stillSprite) {
        ResourceLocation frozenLocation = new ResourceLocation(BCLib.MODID, "fluid_" + registryName.toString().replace(':', '_') + "_convert_frozen");
        return SpriteFluidFrozen.createSpriteContents(stillSprite, registryName, frozenLocation);
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
     * @see #renderFluid(FluidSpriteType, FluidStack, double, double, Vec3, Vec3, PoseStack.Pose, VertexConsumer, boolean[]) */
    public static void renderFluid(FluidSpriteType type, IFluidTank tank, Vec3 min, Vec3 max, PoseStack.Pose pose, VertexConsumer bbIn,
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
            Vec3 min,
            Vec3 max,
//            BufferBuilder bbIn,
            PoseStack.Pose pose,
            VertexConsumer bbIn,
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
     * @param poseIn The {@link PoseStack.Pose}
     * @param bbIn The {@link VertexConsumer} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     * faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns. */
    public static void renderFluid(
            FluidSpriteType type,
            FluidStack fluid,
            double amount,
            double cap,
            Vec3 min,
            Vec3 max,
            PoseStack.Pose poseIn,
            VertexConsumer bbIn,
            boolean[] sideRender
    ) {
        if (fluid == null || fluid.getRawFluid() == null || amount <= 0) {
            return;
        }
        ProfilerFiller prof = Minecraft.getInstance().getProfiler();
        prof.push("fluid");
        if (sideRender == null) {
            sideRender = DEFAULT_FACES;
        }

//        double height = MathHelper.clamp(amount / cap, 0, 1);
        double height = Mth.clamp(amount / cap, 0, 1);
        final Vec3 realMin, realMax;
        if (fluid.getRawFluid().getFluidType().isLighterThanAir()) {
            realMin = VecUtil.replaceValue(min, Axis.Y, MathUtil.interp(1 - height, min.y, max.y));
            realMax = max;
        } else {
            realMin = min;
            realMax = VecUtil.replaceValue(max, Axis.Y, MathUtil.interp(height, min.y, max.y));
        }

        FluidRenderer.bb = bbIn;
//        FluidRenderer.poseStack = poseStack;
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

        vertex.colouri(RenderUtil.swapARGBforABGR(FluidUtilBC.getColor(fluid.getRawFluid())));

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
            return SpriteUtil.missingSprite().get();
        }
        TextureAtlasSprite s = fluidSprites.get(type).get(FluidUtilBC.getRegistryName(fluid).toString());
        return s != null ? s : SpriteUtil.missingSprite().get();
    }

    /** Helper function to add a vertex. */
    private static void vertex(double x, double y, double z) {
        vertex.positiond(x, y, z);
        texmap.apply(x - xTexDiff, y - yTexDiff, z - zTexDiff);
        vertex.renderAsBlock(pose, bb);
    }

    /** Fills up the given region with the fluids texture, repeated. Ignores the value of {@link FluidStack#getAmount()}. Use
     * {@link GuiUtil}'s fluid drawing methods in preference to this. */
    public static void drawFluidForGui(FluidStack fluid, double startX, double startY, double endX, double endY, GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();

        sprite = FluidRenderer.fluidSprites.get(FluidSpriteType.STILL).get(FluidUtilBC.getRegistryName(fluid.getRawFluid()).toString());
        if (sprite == null) {
//            sprite = Minecraft.getInstance().getTextureMapBlocks().getMissingSprite();
            sprite = SpriteUtil.missingSprite().get();
        }
//        Minecraft.getInstance().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        SpriteUtil.bindTexture(TextureAtlas.LOCATION_BLOCKS);
//        RenderUtil.setGLColorFromInt(fluid.getFluid().getColor(fluid));
        RenderUtil.setGLColorFromInt(FluidUtilBC.getColor(fluid.getRawFluid()));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        Tessellator tess = Tessellator.getInstance();
        Tesselator tess = Tesselator.getInstance();
//        bb = tess.getBuffer();
        bb = tess.getBuilder();
//        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        ((BufferBuilder) bb).begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

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
        public final Vec3 min;
        public final Vec3 max;

        public TankSize(int sx, int sy, int sz, int ex, int ey, int ez) {
            this(new Vec3(sx, sy, sz).scale(1 / 16.0), new Vec3(ex, ey, ez).scale(1 / 16.0));
        }

        public TankSize(Vec3 min, Vec3 max) {
            this.min = min;
            this.max = max;
        }

        public TankSize shrink(double by) {
            return shrink(by, by, by);
        }

        public TankSize shrink(double x, double y, double z) {
            return new TankSize(min.add(x, y, z), max.subtract(x, y, z));
        }

        public TankSize shink(Vec3 by) {
            return shrink(by.x, by.y, by.z);
        }

        public TankSize rotateY() {
            Vec3 _min = rotateY(min);
            Vec3 _max = rotateY(max);
            return new TankSize(VecUtil.min(_min, _max), VecUtil.max(_min, _max));
        }

        private static Vec3 rotateY(Vec3 vec) {
            return new Vec3(//
                    1 - vec.z, //
                    vec.y, //
                    vec.x//
            );
        }
    }
}
