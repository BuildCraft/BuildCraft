package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;

public enum ModelBlockerItem implements IBakedModel {
    INSTANCE;

    private static MutableQuad[] lastQuads;
    private static List<BakedQuad> lastBaked;

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        MutableQuad[] from = BCTransportModels.BLOCKER.getCutoutQuads();
        if (from != lastQuads) {
            lastQuads = from;
            lastBaked = new ArrayList<>(from.length);
            for (MutableQuad q : from) {
                lastBaked.add(q.toBakedItem());
            }
        }
        return lastBaked;
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
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
