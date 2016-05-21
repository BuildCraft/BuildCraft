package buildcraft.core.lib.client.sprite;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

public class SubSprite extends TextureAtlasSprite {
    private final TextureAtlasSprite icon;
    private float u, v;
    private final int w, h;
    private float uScale, vScale;
    private int iw, ih;

    public SubSprite(TextureAtlasSprite icon, int u, int v) {
        this(icon, u, v, 16, 16);
    }

    public SubSprite(TextureAtlasSprite icon, int u, int v, int w, int h) {
        super(icon.getIconName() + "_SubIcon");
        iw = icon.getIconWidth();
        ih = icon.getIconHeight();
        this.icon = icon;
        this.uScale = icon.getMaxU() - icon.getMinU();
        this.vScale = icon.getMaxV() - icon.getMinV();
        this.u = icon.getMinU() + (this.uScale * u / iw);
        this.v = icon.getMinV() + (this.vScale * v / ih);
        this.w = w;
        this.h = h;
    }

    @Override
    public int getIconWidth() {
        return w;
    }

    @Override
    public int getIconHeight() {
        return h;
    }

    @Override
    public float getMinU() {
        return u;
    }

    @Override
    public float getMaxU() {
        return u + (uScale * w / iw);
    }

    @Override
    public float getInterpolatedU(double uu) {
        return u + (uScale * (float) uu / iw);
    }

    @Override
    public float getMinV() {
        return v;
    }

    @Override
    public float getMaxV() {
        return v + (vScale * h / ih);
    }

    @Override
    public float getInterpolatedV(double vv) {
        return v + (vScale * (float) vv / ih);
    }

    @Override
    public String getIconName() {
        return icon.getIconName();
    }

    public static abstract class DelegateSprite extends TextureAtlasSprite {
        protected final TextureAtlasSprite delegate;

        protected DelegateSprite(TextureAtlasSprite delegate, String spriteName) {
            super(delegate.getIconName() + spriteName);
            this.delegate = delegate;
        }

        // @formatter:off
        @Override public int hashCode() {return delegate.hashCode();}
        @Override public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn) {delegate.initSprite(inX, inY, originInX, originInY, rotatedIn);}
        @Override public void copyFrom(TextureAtlasSprite atlasSpirit){delegate.copyFrom(atlasSpirit);}
        @Override public int getOriginX() {return delegate.getOriginX();}
        @Override public int getOriginY() {return delegate.getOriginY();}
        @Override public int getIconWidth() {return delegate.getIconWidth();}
        @Override public boolean equals(Object obj) {return delegate.equals(obj);}
        @Override public int getIconHeight() {return delegate.getIconHeight();}
        @Override public float getMinU() {return delegate.getMinU();}
        @Override public float getMaxU() {return delegate.getMaxU();}
        @Override public float getInterpolatedU(double u) {return delegate.getInterpolatedU(u);}
        @Override public float getMinV() {return delegate.getMinV();}
        @Override public float getMaxV() {return delegate.getMaxV();}
        @Override public float getInterpolatedV(double v) {return delegate.getInterpolatedV(v);}
        @Override public String getIconName() {return delegate.getIconName();}
        @Override public void updateAnimation() {delegate.updateAnimation();}
        @Override public int[][] getFrameTextureData(int index) {return delegate.getFrameTextureData(index);}
        @Override public int getFrameCount() {return delegate.getFrameCount();}
        @Override public void setIconWidth(int newWidth) {delegate.setIconWidth(newWidth);}
        @Override public void setIconHeight(int newHeight) {delegate.setIconHeight(newHeight);}
        @Override public void loadSprite(BufferedImage[] images, AnimationMetadataSection meta) throws IOException {delegate.loadSprite(images, meta);}
        @Override public void generateMipmaps(int level) {delegate.generateMipmaps(level);}
        @Override public void clearFramesTextureData() {delegate.clearFramesTextureData();}
        @Override public boolean hasAnimationMetadata() {return delegate.hasAnimationMetadata();}
        @Override public void setFramesTextureData(List<int[][]> newFramesTextureData) {delegate.setFramesTextureData(newFramesTextureData);}
        @Override public String toString() {return delegate.toString();}
        @Override public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {return delegate.hasCustomLoader(manager, location);}
        @Override public boolean load(IResourceManager manager, ResourceLocation location) {return delegate.load(manager, location);}
        // @formatter:on
    }

    public static class FlippedU extends DelegateSprite {
        public FlippedU(TextureAtlasSprite delegate) {
            super(delegate, "_Flipped_U");
        }

        @Override
        public float getMinU() {
            return delegate.getMaxU();
        }

        @Override
        public float getMaxU() {
            return delegate.getMinU();
        }

        @Override
        public float getInterpolatedU(double u) {
            return delegate.getInterpolatedU(16 - u);
        }
    }

    public static class FlippedV extends DelegateSprite {
        public FlippedV(TextureAtlasSprite delegate) {
            super(delegate, "_Flipped_V");
        }

        @Override
        public float getMinV() {
            return delegate.getMaxV();
        }

        @Override
        public float getMaxV() {
            return delegate.getMinV();
        }

        @Override
        public float getInterpolatedV(double v) {
            return delegate.getInterpolatedV(16 - v);
        }
    }
}
