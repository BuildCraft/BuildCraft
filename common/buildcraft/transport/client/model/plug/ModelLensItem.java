package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.item.ItemPluggableLens.LensData;

public enum ModelLensItem implements IBakedModel {
    INSTANCE;

    private static final List<List<BakedQuad>> cached = new ArrayList<>(34);

    public static void onModelBake() {
        cached.clear();
    }

    private static List<BakedQuad> getQuads(int damage) {
        if (damage < 0 || damage >= 34) damage = 0;
        if (cached.isEmpty()) {
            for (int i = 0; i < 34; i++) {
                List<BakedQuad> list = new ArrayList<>();
                LensData data = new LensData(i);
                MutableQuad[] cutout, translucent;
                EnumFacing side = EnumFacing.WEST;
                if (data.isFilter) {
                    cutout = BCTransportModels.getFilterCutoutQuads(side, data.colour);
                    translucent = BCTransportModels.getFilterTranslucentQuads(side, data.colour);
                } else {
                    cutout = BCTransportModels.getLensCutoutQuads(side, data.colour);
                    translucent = BCTransportModels.getLensTranslucentQuads(side, data.colour);
                }
                for (MutableQuad q : cutout) {
                    list.add(q.toBakedItem());
                }
                for (MutableQuad q : translucent) {
                    list.add(q.toBakedItem());
                }
                cached.add(list);
            }
        }
        return cached.get(damage);
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
        return LensOverride.LENS_OVERRIDE;
    }

    public static class LensOverride extends ItemOverrideList {
        public static final LensOverride LENS_OVERRIDE = new LensOverride();

        private LensOverride() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            return new ModelItemSimple(getQuads(stack.getItemDamage()), ModelItemSimple.TRANSFORM_PLUG_AS_ITEM);
        }
    }
}
