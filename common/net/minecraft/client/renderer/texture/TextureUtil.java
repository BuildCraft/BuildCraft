// STUB(R.Chen): 1.12 TextureUtil — removed in 1.20.1.
package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public class TextureUtil {
    public static int glGenTextures() { return 0; }
    public static void uploadTextureImage(int textureId, BufferedImage image) {}
    public static void uploadTextureImageAllocate(int textureId, BufferedImage image, boolean blur, boolean clamp) {}
    public static BufferedImage readBufferedImage(InputStream inputStream) throws Exception { return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); }
}
