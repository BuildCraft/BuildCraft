/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.misc;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Shims for GlStateManager methods that were removed in 1.20.1 (fixed-function pipeline removed).
 * TODO(R.Chen): files using these should be fully rewritten with modern render pipeline.
 */
public class GlStateManagerCompat {
    // Fixed-function attribute stack — no equivalent in core OpenGL 3.2+
    public static void pushAttrib() {}
    public static void popAttrib() {}

    // Matrix mode / matrix stack — replaced by MatrixStack
    public static void matrixMode(int mode) {}
    public static void loadIdentity() {}
    public static void pushMatrix() { RenderSystem.getModelViewStack().push(); }
    public static void popMatrix() { RenderSystem.getModelViewStack().pop(); }

    // Rescale normal — removed
    public static void enableRescaleNormal() {}
    public static void disableRescaleNormal() {}

    // Shade model — default smooth in core profile
    public static void shadeModel(int mode) {}

    // Viewport
    public static void viewport(int x, int y, int width, int height) {
        GlStateManager._viewport(x, y, width, height);
    }

    // Bind texture
    public static void bindTexture(int texture) {
        GlStateManager._bindTexture(texture);
    }

    // Line width
    public static void glLineWidth(float width) {
        RenderSystem.lineWidth(width);
    }

    // Polygon mode — not available in GLES / WebGL; stub
    public static void glPolygonMode(int face, int mode) {
        // TODO(R.Chen): GL11.glPolygonMode(face, mode) if on desktop GL
    }

    // Get integer state
    public static int glGetInteger(int pname) {
        return org.lwjgl.opengl.GL11.glGetInteger(pname);
    }

    // Get float state
    public static void getFloat(int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetFloatv(pname, params);
    }
}
