// STUB(R.Chen): Minecraft 1.12 OpenGlHelper — removed in 1.20. TODO: use RenderSystem/GlStateManager directly.
package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;

public class OpenGlHelper {
    public static final int lightmapTexUnit = 1;
    public static boolean shadersSupported = false;

    public static void setLightmapTextureCoords(int target, float x, float y) {
        // TODO(R.Chen): migrate to shader uniforms
    }
}
