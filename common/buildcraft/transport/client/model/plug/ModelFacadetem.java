package buildcraft.transport.client.model.plug;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import buildcraft.transport.item.ItemPluggableFacade;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.TimeUnit;

public enum ModelFacadetem implements IBakedModel {
    INSTANCE;

    private static final LoadingCache<KeyPlugFacade, IBakedModel> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(CacheLoader.from(key -> new ModelItemSimple(PlugBakerFacade.INSTANCE.bake(key), ModelItemSimple.TRANSFORM_PLUG_AS_ITEM)));

    public static void onModelBake() {
        cache.cleanUp();
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return ImmutableList.of();
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
        return FacadeOverride.FACADE_OVERRIDE;
    }

    public static class FacadeOverride extends ItemOverrideList {
        public static final FacadeOverride FACADE_OVERRIDE = new FacadeOverride();

        private FacadeOverride() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            IBlockState state = ItemPluggableFacade.getState(
                    stack,
                    Minecraft.getMinecraft().world,
                    BlockPos.ORIGIN,
                    EnumFacing.NORTH,
                    Minecraft.getMinecraft().player,
                    EnumHand.MAIN_HAND
            );
            boolean isHollow = ItemPluggableFacade.getIsHollow(stack);
            if (state == null) {
                return originalModel;
            }
            return cache.getUnchecked(new KeyPlugFacade(BlockRenderLayer.TRANSLUCENT, EnumFacing.WEST, state, isHollow));
        }
    }
}
