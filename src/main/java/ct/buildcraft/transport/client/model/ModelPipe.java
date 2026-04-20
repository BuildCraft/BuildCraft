/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.model;

import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.transport.block.BlockPipeHolder;
import ct.buildcraft.transport.client.model.PipeModelCacheAll.PipeAllCutoutKey;
import ct.buildcraft.transport.client.model.PipeModelCacheAll.PipeAllTranslucentKey;
import ct.buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import ct.buildcraft.transport.client.model.key.PipeModelKey;
import ct.buildcraft.transport.pipe.Pipe;
import ct.buildcraft.transport.tile.TilePipeHolder;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public enum ModelPipe implements IDynamicBakedModel {
	INSTANCE;

	public static final ModelProperty<TilePipeHolder> PipeTypeModelKey = new ModelProperty<TilePipeHolder>();
//	public static final ModelProperty<?> PipeDataModelKey = new ModelProperty<PipeModelData>();
/*	protected static final Direction[][] LiteraDirection = {
			{ Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST },
			{ Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP },
			{ Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH } };
	protected static final EnumMap<Direction, BakedQuad[]> facesCacheBase = new EnumMap<>(Direction.class);// {up, left,
																											// down,
																											// right,
																											// center,centerHigh}
	protected static final EnumMap<Direction, BakedQuad[]> facesCacheCombine = new EnumMap<>(Direction.class);*/

	protected static final HashMap<ResourceLocation, TextureAtlasSprite> particleIcon = new HashMap<>();
//	protected static final B
	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
			@NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		TilePipeHolder tile = data.get(PipeTypeModelKey);
        if (tile == null || tile.getPipe() == Pipe.EMPTY) {
            if (renderType == RenderType.translucent()) {
                return ImmutableList.of();
            }
            return PipeModelCacheBase.cacheCutout.bake(new PipeBaseCutoutKey(PipeModelKey.DEFAULT_KEY));
        }

        if (renderType == RenderType.translucent()) {
            PipeAllTranslucentKey realKey = new PipeAllTranslucentKey(tile);
            return PipeModelCacheAll.cacheTranslucent.bake(realKey);
        } else {
            PipeAllCutoutKey realKey = new PipeAllCutoutKey(tile);
            return PipeModelCacheAll.cacheCutout.bake(realKey);
        }
		
		
/*		List<BakedQuad> result = new ArrayList<BakedQuad>();
		TilePipeHolder p = (TilePipeHolder) o;
		Pipe pipe = p.getPipe();

		float[] conSize = { pipe.getConnectedDist(Direction.DOWN), pipe.getConnectedDist(Direction.UP),
				pipe.getConnectedDist(Direction.NORTH), pipe.getConnectedDist(Direction.SOUTH),
				pipe.getConnectedDist(Direction.WEST), pipe.getConnectedDist(Direction.EAST) };
		boolean[] isCon = { conSize[0] > 0, conSize[1] > 0, conSize[2] > 0, conSize[3] > 0, conSize[4] > 0,
				conSize[5] > 0 };
		boolean[] isTransfix = { isCon[0] && isCon[1], isCon[2] && isCon[3], isCon[4] && isCon[5] };
		int indexOff = -1;

		if (isTransfix[1]) indexOff = 2;// North and South
		if (isTransfix[2]) indexOff = 4;// West and East

//    		flag = 8;
		result.add(facesCacheBase.get(Direction.from3DDataValue(indexOff))[5]);
		result.add(facesCacheBase.get(Direction.from3DDataValue(indexOff + 1))[5]);

		for (int i = 0; i < 4; i++) {
			if (isCon[i]) {
				result.add(facesCacheBase.get(Direction.from3DDataValue((i + indexOff) % 6))[5]);
				result.add(facesCacheBase.get(Direction.from3DDataValue((i + indexOff) % 6))[0]);
				result.add(facesCacheBase.get(Direction.from3DDataValue((i + indexOff) % 6))[1]);
				result.add(facesCacheBase.get(Direction.from3DDataValue((i + indexOff) % 6))[2]);
				result.add(facesCacheBase.get(Direction.from3DDataValue((i + indexOff) % 6))[3]);

				result.add(facesCacheBase.get(Direction.from3DDataValue(indexOff))[i]);
				result.add(facesCacheBase.get(Direction.from3DDataValue(indexOff + 1))[i]);
			} else
				result.add(facesCacheCombine.get(Direction.from3DDataValue(indexOff))[i]);
		}
		return result;*/

		/*
		 * for(int i=0;i<6;i++) {//default if(isCon[i]) {
		 * result.add(facesCacheBase.get(Direction.from3DDataValue(i))[0]);
		 * result.add(facesCacheBase.get(Direction.from3DDataValue(i))[1]);
		 * result.add(facesCacheBase.get(Direction.from3DDataValue(i))[2]);
		 * result.add(facesCacheBase.get(Direction.from3DDataValue(i))[3]); } else
		 * result.add(facesCacheBase.get(Direction.from2DDataValue(i))[5]); }
		 */

	}
	

	/*
	 * baked the model with no texture, as the texture must be put in during
	 * rendering
	 **/
	
	@Override
	public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
			@NotNull ModelData data) {
		return ChunkRenderTypeSet.of(List.of(RenderType.cutout(), RenderType.translucent()));
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return SpriteUtil.missingSprite();
	}
	
	/**
	 * @see BlockPipeHolder#addDestroyEffects(BlockState, Level, BlockPos, ParticleEngine)
	 * */
	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {//TODO
		if(data == ModelData.EMPTY)
			return SpriteUtil.missingSprite();
		ResourceLocation identifier = data.get(PipeTypeModelKey).getPipe().definition.identifier;
//		return particleIcon.computeIfAbsent(new ResourceLocation(identifier.getNamespace(), "pipes/"+identifier.getPath()),
//				Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)::apply);
		return particleIcon.computeIfAbsent(new ResourceLocation(identifier.getNamespace(), "pipes/"+identifier.getPath()),
				(a) -> {
					TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(a);
					if(sprite == SpriteUtil.missingSprite())
						BCLog.logger.error("ModelPipe:empty spite for "+a);
					return sprite;
				});
	}

	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

	/*
	 * public static class PipeModelData{ protected final Float[] conSizes;
	 * protected final Float[] uvs;
	 * 
	 * public PipeModelData(Float[] conSizes) { this.conSizes = conSizes; uvs =
	 * null; } public PipeModelData(Float[] conSizes, Float[] UVs) { this.conSizes =
	 * conSizes; uvs = UVs; } }
	 */

}
