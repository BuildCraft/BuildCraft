package buildcraft.lib.client.sprite;

public interface ISprite {
    void bindTexture();

    /** @param u A value between 0 and 1
     * @return */
    double getInterpU(double u);

    /** @param v A value between 0 and 1
     * @return */
    double getInterpV(double v);

    default ISprite subRelative(int u, int v, int width, int height, int spriteSize) {
        double size = spriteSize;
        return subRelative(u / size, v / size, width / size, height / size);
    }

    default ISprite subAbsolute(int uMin, int vMin, int uMax, int vMax, int spriteSize) {
        double size = spriteSize;
        return subAbsolute(uMin / size, vMin / size, uMax / size, vMax / size);
    }

    default ISprite subRelative(double u, double v, double width, double height) {
        return subAbsolute(u, v, u + width, v + height);
    }

    default ISprite subAbsolute(double uMin, double vMin, double uMax, double vMax) {
        return new SubSprite(this, uMin, vMin, uMax, vMax);
    }

    default SpriteNineSliced slice(int uMin, int vMin, int uMax, int vMax, int textureSize) {
        return new SpriteNineSliced(this, uMin, vMin, uMax, vMax, textureSize);
    }

    default SpriteNineSliced slice(double uMin, double vMin, double uMax, double vMax, double scale) {
        return new SpriteNineSliced(this, uMin, vMin, uMax, vMax, scale);
    }
}
