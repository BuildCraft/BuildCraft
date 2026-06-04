// STUB(R.Chen): Sprite compat helpers for 1.12.2 → 1.20.1 migration.
package buildcraft.lib.compat;

import net.minecraft.client.texture.Sprite;

/** Provides 1.12.2 Sprite API stubs not present in 1.20.1. */
public final class SpriteCompat {
    private SpriteCompat() {}

    /** 1.12.2: buildcraft.lib.compat.SpriteCompat.getFrameCount(sprite) — stubs to 0 in 1.20.1. */
    public static int getFrameCount(Sprite sprite) {
        return 0; // TODO(migration): access via SpriteContents.getDistinctFrameCount()
    }

    /** 1.12.2: buildcraft.lib.compat.SpriteCompat.getFrameTextureData(sprite, frame) — stubs to empty array in 1.20.1. */
    public static int[][] getFrameTextureData(Sprite sprite, int frame) {
        return new int[0][0]; // TODO(migration): access via SpriteContents.mipmapLevelsImages
    }

    /** 1.12.2: buildcraft.lib.compat.SpriteCompat.getOriginX(sprite) — stubs to 0. */
    public static int getOriginX(Sprite sprite) { return sprite.getX(); }

    /** 1.12.2: buildcraft.lib.compat.SpriteCompat.getOriginY(sprite) — stubs to 0. */
    public static int getOriginY(Sprite sprite) { return sprite.getY(); }
}
