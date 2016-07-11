package buildcraft.transport;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BCTransportSprites {
    public static final SpriteHolder SPRITE_FILTERED_BUFFER;

    static {
        SPRITE_FILTERED_BUFFER = getSprite("gui/empty_filtered_buffer_slot");
    }

    private static SpriteHolder getSprite(String loc) {
        return SpriteHolderRegistry.getHolder("buildcrafttransport:" + loc);
    }
}
