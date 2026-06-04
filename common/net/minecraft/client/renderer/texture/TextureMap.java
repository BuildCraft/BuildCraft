// STUB(R.Chen): TextureMap → SpriteAtlasTexture compat shim for 1.12.2 → 1.20.1 migration
package net.minecraft.client.renderer.texture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

/**
 * Compat shim: wraps SpriteAtlasTexture and exposes the 1.12.2 TextureMap API surface
 * needed by BuildCraft. Returned by TextureStitchEvent.getMap().
 */
public class TextureMap {
    private final SpriteAtlasTexture atlas;

    public TextureMap(SpriteAtlasTexture atlas) {
        this.atlas = atlas;
    }

    public SpriteAtlasTexture getAtlas() { return atlas; }

    /** 1.12.2: registerSprite → in 1.20.1 we just note it; actual registration via TextureStitchEvent.Pre.addSprite() */
    public Sprite registerSprite(Identifier location) {
        return atlas.getSprite(location);
    }

    /** 1.12.2: setTextureEntry → no-op stub for compile compat */
    public boolean setTextureEntry(Object sprite) {
        return true; // no-op
    }

    /** 1.12.2: getAtlasSprite → SpriteAtlasTexture.getSprite() */
    public Sprite getAtlasSprite(String location) {
        return atlas.getSprite(new Identifier(location));
    }

    /** 1.12.2: getMissingSprite */
    public Sprite getMissingSprite() {
        return MinecraftClient.getInstance()
                .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                .apply(MissingSprite.getMissingSpriteId());
    }
}
