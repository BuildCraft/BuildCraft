package buildcraft.transport.client.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;
import buildcraft.transport.client.model.key.PipeModelKey;

public enum ModelPipe implements IBakedModel {
    INSTANCE;

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return ImmutableList.of();
        }
        PipeModelKey key = null;
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            key = ext.getValue(BlockPipeHolder.PROP_MODEL);
        }
        if (key == null) {
            key = PipeModelKey.DEFAULT_KEY;
        }

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (layer == BlockRenderLayer.TRANSLUCENT) {
            PipeBaseTransclucentKey realKey = new PipeBaseTransclucentKey(key);
            return PipeModelCacheBase.cacheTranslucent.bake(realKey, MutableQuad.ITEM_LMAP);
        } else {
            PipeBaseCutoutKey realKey = new PipeBaseCutoutKey(key);
            return PipeModelCacheBase.cacheCutout.bake(realKey, MutableQuad.ITEM_LMAP);
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
