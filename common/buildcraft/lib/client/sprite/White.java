package buildcraft.lib.client.sprite;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class White {
    public static final ResourceLocation LOCATION = new ResourceLocation("white");
    private static TextureAtlasSprite instance = null;
    @SuppressWarnings("deprecation")
    public static final TextureAtlasSprite instance()
    {
        if (instance == null)
        {
            instance = new Material(TextureAtlas.LOCATION_BLOCKS, LOCATION).sprite();
        }
        return instance;
    }
}
