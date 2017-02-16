package buildcraft.transport.client.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.config.DetailedConfigOption;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;

public interface IPipeBaseModelGen {
    public static final DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.725");

    List<BakedQuad> generateCutout(PipeBaseCutoutKey key);

    List<BakedQuad> generateTranslucent(PipeBaseTransclucentKey key);

    /** Gets a sprite that can be baked into the item model. */
    TextureAtlasSprite getItemSprite(PipeDefinition def, int index);

    void onTextureStitchPre(TextureMap map);
}
