package buildcraft.transport;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.transport.client.model.PipeModelCacheAll;
import buildcraft.transport.client.model.PipeModelCacheBase;

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

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCTransportSprites.class);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        PipeModelCacheBase.generator.onTextureStitchPre(event.getMap());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        PipeModelCacheAll.clearModels();
    }
}
