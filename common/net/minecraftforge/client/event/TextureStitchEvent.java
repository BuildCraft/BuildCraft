// STUB(R.Chen): Forge TextureStitchEvent — compile shim.
package net.minecraftforge.client.event;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.texture.SpriteAtlasTexture;

public class TextureStitchEvent {
    private final TextureMap map;
    public TextureStitchEvent(SpriteAtlasTexture atlas) { this.map = new TextureMap(atlas); }
    public TextureMap getMap() { return map; }

    public static class Pre extends TextureStitchEvent {
        public Pre(SpriteAtlasTexture atlas) { super(atlas); }
        public void addSprite(net.minecraft.util.Identifier location) {}
    }

    public static class Post extends TextureStitchEvent {
        public Post(SpriteAtlasTexture atlas) { super(atlas); }
    }
}
