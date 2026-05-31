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

import net.minecraft.client.MinecraftClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GlUtil {
    private static ByteBuffer depthBuffer = null;

    public static void saveDepthBuffer() {
        MinecraftClient.getInstance().getProfiler().push("Save depth buffer");
        depthBuffer = BufferUtils.createByteBuffer(
            MinecraftClient.getInstance().displayWidth
                * MinecraftClient.getInstance().displayHeight
                * Float.BYTES
        );
        GL11.glReadPixels(
            0,
            0,
            MinecraftClient.getInstance().displayWidth,
            MinecraftClient.getInstance().displayHeight,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_FLOAT,
            depthBuffer
        );
        MinecraftClient.getInstance().getProfiler().pop();
    }

    public static void restoreDepthBuffer() {
        MinecraftClient.getInstance().getProfiler().push("Restore depth buffer");
        GL11.glColorMask(false, false, false, false);
        GL11.glRasterPos2i(0, 0);
        GL14.glWindowPos2i(0, 0);
        GL11.glDrawPixels(
            MinecraftClient.getInstance().displayWidth,
            MinecraftClient.getInstance().displayHeight,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_FLOAT,
            depthBuffer
        );
        depthBuffer = null;
        GL11.glColorMask(true, true, true, true);
        MinecraftClient.getInstance().getProfiler().pop();
    }
}
