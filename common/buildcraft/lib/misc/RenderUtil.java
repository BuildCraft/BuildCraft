/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class RenderUtil {

    private static final ThreadLocal<TessellatorQueue> threadLocalTessellators;
    // private static final MethodHandle HANDLE_FORGE_TESSELLATOR;
    // 1.16.5+: BufferBuilder provides getter
    // private static final MethodHandle HANDLE_IS_BUFFER_DRAWING;

    static {
        threadLocalTessellators = ThreadLocal.withInitial(TessellatorQueue::new);
//        HANDLE_FORGE_TESSELLATOR = createGetter(TileEntityRendererDispatcher.class, Tessellator.class, "batchBuffer");
//        HANDLE_IS_BUFFER_DRAWING = createGetter(BufferBuilder.class, boolean.class, "isDrawing", "field_179010_r");
    }

    private static MethodHandle createGetter(Class<?> owner, Class<?> type, String... names) {
        try {
            Set<String> nameSet = new HashSet<>();
            Collections.addAll(nameSet, names);
            List<Field> validFields = new ArrayList<>();
            for (Field field : owner.getDeclaredFields()) {
                if (field.getType() == type && nameSet.contains(field.getName())) {
                    validFields.add(field);
                }
            }

            if (validFields.size() != 1) {
                throw new Error("Incorrect number of fields! (Expected 1, but got " + validFields + ")");
            }
            Field fld = validFields.get(0);
            fld.setAccessible(true);
            return MethodHandles.publicLookup().unreflectGetter(fld);
        } catch (ReflectiveOperationException roe) {
            throw new Error("Failed to obtain forge's batch buffer!", roe);
        }
    }

    public static void registerBlockColour(@Nullable Block block, BlockColor colour) {
        if (block != null) {
            Minecraft.getInstance().getBlockColors().register(colour, block);
        }
    }

    public static void registerItemColour(@Nullable Item item, ItemColor colour) {
        if (item != null) {
            Minecraft.getInstance().getItemColors().register(colour, item);
        }
    }

    /** Takes _RGB (alpha is set to 1) */
    public static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

//        GlStateManager.color(red, green, blue);
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
    }

    /** Takes ARGB */
    public static void setGLColorFromIntPlusAlpha(int color) {
        float alpha = (color >>> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

//        GlStateManager.color(red, green, blue, alpha);
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public static int swapARGBforABGR(int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = (argb >> 0) & 255;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static int combineWithFluidLight(int combinedLight, byte fluidLight) {
        return (combinedLight & 0xFFFF0000) | Math.max(fluidLight << 4, combinedLight & 0xFFFF);
    }

    // Calen
    public static int getCombinedLight(Level level, BlockPos pos) {
        byte sky = (byte) level.getLightEngine().getRawBrightness(pos, 0);
        byte block = (byte) level.getLightEmission(pos);
        return (sky << 20) | (block << 4);
    }

    public static byte getSkyLightFromCombined(int combinedLight) {
        return (byte) ((combinedLight & 0xFFFF0000) >>> 20);
    }

    public static byte getBlockLightFromCombined(int combinedLight) {
        return (byte) ((combinedLight & 0x0000FFFF) >>> 4);
    }

    public static boolean isRenderingTranslucent() {
//        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT || MinecraftForgeClient.getRenderPass() == 1;
        return MinecraftForgeClient.getRenderType() == RenderType.translucent();
    }

    /** @return true if this thread is the main minecraft thread, used for all client side game logic and (by default)
     *         tile entity rendering. */
    public static boolean isMainRenderThread() {
        return Minecraft.getInstance().renderOnThread();
    }

    /** @return The first unused {@link Tesselator} for the current thread that uses the given vertex format. (Unused =
     *         {@link #isDrawing(BufferBuilder)} returns false). */
    public static AutoTessellator getThreadLocalUnusedTessellator() {
        return threadLocalTessellators.get().nextFreeTessellator();
    }

//    /** @return The forge {@link Tesselator} used for rendering {@link BlockEntityRenderer}'s. */
//    public static Tessellator getMainTessellator() {
//        if (!isMainRenderThread()) {
//            throw new IllegalStateException("Not the main thread!");
//        }
//        try {
//            return (Tessellator) HANDLE_FORGE_TESSELLATOR.invokeExact(TileEntityRendererDispatcher.instance);
//        } catch (Throwable t) {
//            throw new Error(t);
//        }
//    }

    /** @return True if the given {@link BufferBuilder} is currently in the middle of drawing. Essentially returns true
     *         if {@link BufferBuilder#begin(VertexFormat.Mode, VertexFormat)} would throw an exception. */
    public static boolean isDrawing(BufferBuilder bb) {
//        try {
//            return (boolean) HANDLE_IS_BUFFER_DRAWING.invokeExact(bb);
//        } catch (Throwable t) {
//            throw new Error(t);
//        }
        return bb.building();
    }

    private static Tesselator newTessellator() {
        // The same as what minecraft expands a tessellator by
        return new Tesselator(0x200_000);
    }

    // Calen add
    // 1.12.2 GlStateManager#color
    public static void color(float colorRed, float colorGreen, float colorBlue) {
        color(colorRed, colorGreen, colorBlue, 1.0F);
    }

    public static void color(float colorRed, float colorGreen, float colorBlue, float alpha) {
        RenderSystem.setShaderColor(colorRed, colorGreen, colorBlue, alpha);
    }

    // Calen
    public static void disableBlend() {
        RenderSystem.disableBlend();
    }

    // Calen
    public static void enableBlend() {
        RenderSystem.enableBlend();
    }

    // Calen
    public static void disableDepth() {
        RenderSystem.disableDepthTest();
    }

    // Calen
    public static void enableDepth() {
        RenderSystem.enableDepthTest();
    }

    // Calen

    /** Sets OpenGL lighting for rendering blocks as items inside GUI screens (such as containers). */
    public static void enableGUIStandardItemLighting() {
        // Calen: maybe not right
        Lighting.setupFor3DItems();
        // from 1.12.2 RenderHelper.class
//        GlStateManager.pushMatrix();
//        poseStack.pushPose();
//        // Calen: FATAL ERROR in native method: Thread[Render thread,10,main]: No context is current or a function that is not available in the current context was called
//        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);
//        enableStandardItemLighting();
//        GlStateManager.popMatrix();
//        poseStack.popPose();
    }

    // Calen

    /** Disables the OpenGL lighting properties enabled by enableStandardItemLighting */
    public static void disableStandardItemLighting() {
        // TODO Calen disableStandardItemLighting???
//        GlStateManager.disableLighting();
//        GlStateManager.disableLight(0);
//        GlStateManager.disableLight(1);
//        GlStateManager.disableColorMaterial();
    }

    public static void enableAlpha() {
        RenderSystem.colorMask(true, true, true, true);
    }

    public static void disableAlpha() {
        RenderSystem.colorMask(true, true, true, false);
    }

    static class TessellatorQueue {
        // Max size of 20: if we go over this then something has gone very wrong
        // In theory this shouldn't even go above about 3.
        private static final int BUFFER_COUNT = 20;

        final Tesselator[] tessellators = new Tesselator[BUFFER_COUNT];
        final boolean[] tessellatorInUse = new boolean[BUFFER_COUNT];

        AutoTessellator nextFreeTessellator() {
            for (int i = 0; i < tessellators.length; i++) {
                if (tessellatorInUse[i]) {
                    continue;
                }
                Tesselator tess = tessellators[i];
                if (tess == null) {
                    tess = newTessellator();
                    tessellators[i] = tess;
                }
                return new AutoTessellator(this, i);
            }
            /* Assume something has gone wrong as it seems quite odd to have this many buffers rendering at the same
             * time. */
            throw new Error("Too many tessellators! Has a caller not finished with one of them?");
        }
    }

    public static final class AutoTessellator implements AutoCloseable {
        private final TessellatorQueue queue;
        private final int index;
        public final Tesselator tessellator;

        public AutoTessellator(TessellatorQueue queue, int index) {
            this.queue = queue;
            this.index = index;
            this.tessellator = queue.tessellators[index];
            queue.tessellatorInUse[index] = true;
        }

        @Override
        public void close() {
            queue.tessellatorInUse[index] = false;
        }
    }

    /** {@link ForgeRenderTypes.CustomizableTextureState} is private, so we create our own. */
    public static class BCCustomizableTextureState extends RenderStateShard.TextureStateShard {
        public BCCustomizableTextureState(ResourceLocation resLoc, Supplier<Boolean> blur, Supplier<Boolean> mipmap) {
            super(resLoc, blur.get(), mipmap.get());
            this.setupState = () -> {
                this.blur = blur.get();
                this.mipmap = mipmap.get();
                TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
                texturemanager.getTexture(resLoc).setFilter(this.blur, this.mipmap);
                RenderSystem.setShaderTexture(0, resLoc);
            };
        }
    }
}
