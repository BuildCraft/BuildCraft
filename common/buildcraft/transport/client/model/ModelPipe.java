package buildcraft.transport.client.model;

import java.lang.ref.WeakReference;
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

import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.client.model.PipeModelCacheAll.PipeAllCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheAll.PipeAllTranslucentKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.tile.TilePipeHolder;

public enum ModelPipe implements IBakedModel {
    INSTANCE;

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return ImmutableList.of();
        }

        TilePipeHolder tile = null;
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            WeakReference<TilePipeHolder> ref = ext.getValue(BlockPipeHolder.PROP_TILE);
            if (ref != null) {
                tile = ref.get();
            }
        }

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (tile == null) {
            if (layer == BlockRenderLayer.TRANSLUCENT) {
                return ImmutableList.of();
            }
            return PipeModelCacheBase.cacheCutout.bake(new PipeBaseCutoutKey(PipeModelKey.DEFAULT_KEY));
        }

        if (layer == BlockRenderLayer.TRANSLUCENT) {
            PipeAllTranslucentKey realKey = new PipeAllTranslucentKey(tile);
            return PipeModelCacheAll.cacheTranslucent.bake(realKey);
        } else {
            PipeAllCutoutKey realKey = new PipeAllCutoutKey(tile);
            return PipeModelCacheAll.cacheCutout.bake(realKey);
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
