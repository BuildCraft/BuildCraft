/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.client.model.PipeModelCacheAll.PipeAllCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheAll.PipeAllTranslucentKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.tile.TilePipeHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public enum ModelPipe implements BakedModel {
    INSTANCE;

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    /**
     * @param extraData Defined by {@link TilePipeHolder#getModelData()} in 1.18.2
     */
    @NotNull
    @Override
//    public List<BakedQuad> getQuads(IBlockState state, Direction side, long rand)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        if (side != null) {
            return ImmutableList.of();
        }

//        TilePipeHolder tile = null;
//        if (state instanceof IExtendedBlockState) {
//            IExtendedBlockState ext = (IExtendedBlockState) state;
//            WeakReference<TilePipeHolder> ref = ext.getValue(BlockPipeHolder.PROP_TILE);
//            if (ref != null) {
//                tile = ref.get();
//            }
//        }
        TilePipeHolder tile = extraData.getData(BlockPipeHolder.PROP_TILE);

//        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        RenderType layer = MinecraftForgeClient.getRenderType();

        if (tile == null || tile.getPipe() == null) {
            if (layer == RenderType.translucent()) {
                return ImmutableList.of();
            }
            return PipeModelCacheBase.cacheCutout.bake(new PipeBaseCutoutKey(PipeModelKey.DEFAULT_KEY));
        }

        // Calen:if only bake translucent, the colorless pipe texture will disappear
////        if (layer == BlockRenderLayer.TRANSLUCENT)
//        if (layer == RenderType.translucent()) {
//            PipeAllTranslucentKey realKey = new PipeAllTranslucentKey(tile);
//            return PipeModelCacheAll.cacheTranslucent.bake(realKey);
//        } else {
//            PipeAllCutoutKey realKey = new PipeAllCutoutKey(tile);
//            return PipeModelCacheAll.cacheCutout.bake(realKey);
//        }

        List<BakedQuad> translucent = PipeModelCacheAll.cacheTranslucent.bake(new PipeAllTranslucentKey(tile));
        List<BakedQuad> cutout = PipeModelCacheAll.cacheCutout.bake(new PipeAllCutoutKey(tile));
        List<BakedQuad> ret = Lists.newArrayList();
        ret.addAll(translucent);
        ret.addAll(cutout);
        return ret;
    }

    @Override
//    public boolean isAmbientOcclusion()
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
//    public boolean isBuiltInRenderer()
    public boolean isCustomRenderer() {
        return false;
    }

    // Calen: if missingno, the particle when entity falls onto the pipe, the particle will be missingno
    @Override
//    public TextureAtlasSprite getParticleTexture()
    public TextureAtlasSprite getParticleIcon() {
//        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        return SpriteUtil.white();
    }

    @Override
//    public ItemCameraTransforms getItemCameraTransforms()
    public ItemTransforms getTransforms() {
        return ModelItemSimple.TRANSFORM_DEFAULT;
    }

    @Override
//    public ItemOverrideList getOverrides()
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
