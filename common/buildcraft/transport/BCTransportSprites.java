package buildcraft.transport;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BCTransportSprites {
    public static final SpriteHolder EMPTY_FILTERED_BUFFER_SLOT;
    public static final SpriteHolder NOTHING_FILTERED_BUFFER_SLOT;
    public static final SpriteHolder PIPE_COLOUR;

    static {
        EMPTY_FILTERED_BUFFER_SLOT = getSprite("gui/empty_filtered_buffer_slot");
        NOTHING_FILTERED_BUFFER_SLOT = getSprite("gui/nothing_filtered_buffer_slot");
        PIPE_COLOUR = getSprite("pipes/overlay_stained");
    }

    private static SpriteHolder getSprite(String loc) {
        return SpriteHolderRegistry.getHolder("buildcrafttransport:" + loc);
    }

    public static void preInit() {
        // noting, just for sprite loading
    }
}
