package buildcraft.lib.misc;

import net.minecraft.client.renderer.GlStateManager;

public class RenderUtil {

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

    @SuppressWarnings("PointlessBitwiseExpression")
    public static int swapARGBforABGR(int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = (argb >> 0) & 255;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

}
