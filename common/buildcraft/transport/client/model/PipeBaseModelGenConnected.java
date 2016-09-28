package buildcraft.transport.client.model;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;

public enum PipeBaseModelGenConnected implements IPipeBaseModelGen {
    INSTANCE;

    // Textures
    private static final Map<PipeDefinition, TextureAtlasSprite[]> sprites = new IdentityHashMap<>();

    @Override
    public void onTextureStitchPre(TextureMap map) {
        PipeBaseModelGenStandard.INSTANCE.onTextureStitchPre(map);

        // TODO: Custom sprite creation + stitching
    }

    @Override
    public TextureAtlasSprite getItemSprite(PipeDefinition def, int index) {
        return PipeBaseModelGenStandard.INSTANCE.getItemSprite(def, index);
    }

    // Models

    @Override
    public List<BakedQuad> generateTranslucent(PipeBaseTransclucentKey key) {
        return PipeBaseModelGenStandard.INSTANCE.generateTranslucent(key);
    }

    @Override
    public List<BakedQuad> generateCutout(PipeBaseCutoutKey key) {
        return ImmutableList.of();
    }

}
