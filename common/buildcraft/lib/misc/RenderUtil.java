/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.FastTESR;

public class RenderUtil {

    private static final ThreadLocal<TessellatorQueue> threadLocalTessellators;
    private static final MethodHandle HANDLE_FORGE_TESSELLATOR;
    private static final MethodHandle HANDLE_IS_BUFFER_DRAWING;

    static {
        threadLocalTessellators = ThreadLocal.withInitial(TessellatorQueue::new);
        HANDLE_FORGE_TESSELLATOR = createGetter(TileEntityRendererDispatcher.class, Tessellator.class, "batchBuffer");
        HANDLE_IS_BUFFER_DRAWING = createGetter(BufferBuilder.class, boolean.class, "isDrawing", "field_179010_r");
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

    public static void registerBlockColour(@Nullable Block block, IBlockColor colour) {
        if (block != null) {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(colour, block);
        }
    }

    public static void registerItemColour(@Nullable Item item, IItemColor colour) {
        if (item != null) {
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(colour, item);
        }
    }

    /** Takes _RGB (alpha is set to 1) */
    public static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue);
    }

    /** Takes ARGB */
    public static void setGLColorFromIntPlusAlpha(int color) {
        float alpha = (color >>> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static int swapARGBforABGR(int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = (argb >> 0) & 255;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static boolean isRenderingTranslucent() {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT
            || MinecraftForgeClient.getRenderPass() == 1;
    }

    /** @return true if this thread is the main minecraft thread, used for all client side game logic and (by default)
     *         tile entity rendering. */
    public static boolean isMainRenderThread() {
        return Minecraft.getMinecraft().isCallingFromMinecraftThread();
    }

    /** @return The first unused {@link Tessellator} for the current thread that uses the given vertex format. (Unused =
     *         {@link #isDrawing(BufferBuilder)} returns false). */
    public static AutoTessellator getThreadLocalUnusedTessellator() {
        return threadLocalTessellators.get().nextFreeTessellator();
    }

    /** @return The forge {@link Tessellator} used for rendering {@link FastTESR}'s. */
    public static Tessellator getMainTessellator() {
        if (!isMainRenderThread()) {
            throw new IllegalStateException("Not the main thread!");
        }
        try {
            return (Tessellator) HANDLE_FORGE_TESSELLATOR.invokeExact(TileEntityRendererDispatcher.instance);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    /** @return True if the given {@link BufferBuilder} is currently in the middle of drawing. Essentially returns true
     *         if {@link BufferBuilder#begin(int,VertexFormat)} would throw an exception. */
    public static boolean isDrawing(BufferBuilder bb) {
        try {
            return (boolean) HANDLE_IS_BUFFER_DRAWING.invokeExact(bb);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    private static Tessellator newTessellator() {
        // The same as what minecraft expands a tessellator by
        return new Tessellator(0x200_000);
    }

    static class TessellatorQueue {
        // Max size of 20: if we go over this then something has gone very wrong
        // In theory this shouldn't even go above about 3.
        private static final int BUFFER_COUNT = 20;

        final Tessellator[] tessellators = new Tessellator[BUFFER_COUNT];
        final boolean[] tessellatorInUse = new boolean[BUFFER_COUNT];

        AutoTessellator nextFreeTessellator() {
            for (int i = 0; i < tessellators.length; i++) {
                if (tessellatorInUse[i]) {
                    continue;
                }
                Tessellator tess = tessellators[i];
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
        public final Tessellator tessellator;

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
}
