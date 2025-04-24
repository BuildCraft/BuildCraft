package buildcraft.lib.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

// TODO: Implement this!
// (Just a way to make sure people know that RF support is enabled, I'm not sure how detailed this will need to be)
// public class ModelNotifyOfRF implements IBakedModel
public class ModelNotifyOfRF implements IBakedModel {

    final IBakedModel parent;

    public ModelNotifyOfRF(IBakedModel parent) {
        this.parent = parent;
    }

    @Override
    // public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random rand) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    // public boolean isAmbientOcclusion()
    public boolean useAmbientOcclusion() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public boolean isGui3d() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    // public boolean isBuiltInRenderer()
    public boolean isCustomRenderer() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    // public TextureAtlasSprite getParticleTexture()
    public TextureAtlasSprite getParticleIcon() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public ItemOverrideList getOverrides() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
