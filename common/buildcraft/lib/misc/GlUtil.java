/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GlUtil {
    private static ByteBuffer depthBuffer = null;

    public static void saveDepthBuffer() {
        Minecraft.getMinecraft().mcProfiler.startSection("Save depth buffer");
        depthBuffer = BufferUtils.createByteBuffer(
            Minecraft.getMinecraft().displayWidth
                * Minecraft.getMinecraft().displayHeight
                * Float.BYTES
        );
        GL11.glReadPixels(
            0,
            0,
            Minecraft.getMinecraft().displayWidth,
            Minecraft.getMinecraft().displayHeight,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_FLOAT,
            depthBuffer
        );
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    public static void restoreDepthBuffer() {
        Minecraft.getMinecraft().mcProfiler.startSection("Restore depth buffer");
        GL11.glColorMask(false, false, false, false);
        GL11.glRasterPos2i(0, 0);
        GL14.glWindowPos2i(0, 0);
        GL11.glDrawPixels(
            Minecraft.getMinecraft().displayWidth,
            Minecraft.getMinecraft().displayHeight,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_FLOAT,
            depthBuffer
        );
        depthBuffer = null;
        GL11.glColorMask(true, true, true, true);
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
