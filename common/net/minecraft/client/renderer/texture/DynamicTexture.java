// STUB(R.Chen): 1.12 DynamicTexture → net.minecraft.client.texture.NativeImageBackedTexture.
package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;

public class DynamicTexture {
    private final int width;
    private final int height;
    private int[] texturePixels;

    public DynamicTexture(int width, int height) { this.width = width; this.height = height; }
    public DynamicTexture(BufferedImage image) { this.width = image.getWidth(); this.height = image.getHeight(); }

    public void bindTexture() {}
    public void updateDynamicTexture() {}
    public void deleteTexture() {}
    public int[] getTexturePixels() { if (texturePixels == null) texturePixels = new int[width * height]; return texturePixels; }
    public void setTexturePixels(int[] pixels) { this.texturePixels = pixels; }
}
