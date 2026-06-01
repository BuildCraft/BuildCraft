// STUB(R.Chen): Forge TextureStitchEvent — compile shim.
package net.minecraftforge.client.event;

import net.minecraft.client.texture.SpriteAtlasTexture;

public class TextureStitchEvent {
    private final SpriteAtlasTexture map;
    public TextureStitchEvent(SpriteAtlasTexture map) { this.map = map; }
    public SpriteAtlasTexture getMap() { return map; }

    public static class Pre extends TextureStitchEvent {
        public Pre(SpriteAtlasTexture map) { super(map); }
        public void addSprite(net.minecraft.util.Identifier location) {}
    }

    public static class Post extends TextureStitchEvent {
        public Post(SpriteAtlasTexture map) { super(map); }
    }
}
