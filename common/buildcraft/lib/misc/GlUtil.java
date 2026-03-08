/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;

@Deprecated(forRemoval = true)
@OnlyIn(Dist.CLIENT)
public class GlUtil {
    private static ByteBuffer depthBuffer = null;

    public static void saveDepthBuffer() {
        Minecraft.getInstance().getProfiler().push("Save depth buffer");
        depthBuffer = BufferUtils.createByteBuffer(
//            Minecraft.getInstance().displayWidth
                Minecraft.getInstance().getWindow().getWidth()
//                * Minecraft.getInstance().displayHeight
                        * Minecraft.getInstance().getWindow().getHeight()
                        * Float.BYTES
        );
        GL11.glReadPixels(
                0,
                0,
//            Minecraft.getInstance().displayWidth,
                Minecraft.getInstance().getWindow().getWidth(),
//            Minecraft.getInstance().displayHeight,
                Minecraft.getInstance().getWindow().getHeight(),
                GL11.GL_DEPTH_COMPONENT,
                GL11.GL_FLOAT,
                depthBuffer
        );
        Minecraft.getInstance().getProfiler().pop();
    }

    public static void restoreDepthBuffer() {
        Minecraft.getInstance().getProfiler().push("Restore depth buffer");
        GL11.glColorMask(false, false, false, false);
        GL11.glRasterPos2i(0, 0);
        GL14.glWindowPos2i(0, 0);
        GL11.glDrawPixels(
//            Minecraft.getInstance().displayWidth,
                Minecraft.getInstance().getWindow().getWidth(),
//            Minecraft.getInstance().displayHeight,
                Minecraft.getInstance().getWindow().getHeight(),
                GL11.GL_DEPTH_COMPONENT,
                GL11.GL_FLOAT,
                depthBuffer
        );
        depthBuffer = null;
        GL11.glColorMask(true, true, true, true);
        Minecraft.getInstance().getProfiler().pop();
    }
}
