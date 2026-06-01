// STUB(R.Chen): 1.12 PngSizeInfo — removed in 1.20.1.
package net.minecraft.client.renderer.texture;

import java.io.InputStream;

public class PngSizeInfo {
    public final int pngWidth;
    public final int pngHeight;

    public PngSizeInfo(int width, int height) { this.pngWidth = width; this.pngHeight = height; }

    public static PngSizeInfo makeFromInputStream(InputStream inputStream) throws Exception {
        // TODO(R.Chen): read PNG header to get dimensions
        return new PngSizeInfo(16, 16);
    }
}
