package buildcraft.transport;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class TransportSprites {
    public static final SpriteHolder EMPTY_FILTERED_BUFFER_SLOT = SpriteHolderRegistry.getHolder("buildcrafttransport:gui/empty_filtered_buffer_slot");
    public static final SpriteHolder NOTHING_FILTERED_BUFFER_SLOT = SpriteHolderRegistry.getHolder("buildcrafttransport:gui/nothing_filtered_buffer_slot");

    public static void preInit() {
        // noting, just for sprite loading
    }
}
