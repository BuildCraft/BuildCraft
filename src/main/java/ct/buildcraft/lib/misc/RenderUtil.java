/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RenderUtil {

    private static final ThreadLocal<TessellatorQueue> threadLocalTessellators;
    private static final MethodHandle HANDLE_IS_BUFFER_DRAWING;

    static {
        threadLocalTessellators = ThreadLocal.withInitial(TessellatorQueue::new);
        HANDLE_IS_BUFFER_DRAWING = createGetter(BufferBuilder.class, boolean.class, "building", "f_85661_");
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
            Minecraft.getInstance().getBlockColors().register(colour, block);//TODO
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

        RenderSystem.setShaderColor(red, green, blue, 1.0f);//TODO DEBUG
    }

    /** Takes ARGB */
    public static void setGLColorFromIntPlusAlpha(int color) {
        float alpha = (color >>> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public static int swapARGBforRGBA(int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = (argb >> 0) & 255;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    /** @return The first unused {@link Tesselator} for the current thread that uses the given vertex format. (Unused =
     *         {@link #isDrawing(BufferBuilder)} returns false). */
    public static AutoTessellator getThreadLocalUnusedTessellator() {
        return threadLocalTessellators.get().nextFreeTessellator();
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

    private static Tesselator newTessellator() {
        // The same as what minecraft expands a tessellator by
        return new Tesselator(0x200_000);
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
}
