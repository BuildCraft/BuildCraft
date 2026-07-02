package ct.buildcraft.core.client.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.client.model.MutableQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ModelEngine implements IDynamicBakedModel{
	
//	protected static final List<MutableQuad> STATIC = new ArrayList<MutableQuad>();

	public static final ModelProperty<Direction> EngineModelFacingKey = new ModelProperty<Direction>();
	protected static List<MutableQuad> back_faces = null;
	protected static List<MutableQuad> side_faces = null;
	protected static List<MutableQuad> trunk_faces = null;
//	protected final TextureAtlasSprite tex;
	protected final EnumMap<Direction,List<BakedQuad>> faces = new EnumMap<>(Direction.class);
	protected final TextureAtlasSprite particleTex ;
	
	public static void init(BakedModel model) {
		List<MutableQuad> faces = model.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, RenderType.cutoutMipped()).stream().map(MutableQuad::creatByBlock).toList();
		back_faces = faces.stream().filter((a) -> a.getSprite().getName().getPath().equals("blocks/engine/wood/back")).toList();
		side_faces = faces.stream().filter((a) -> a.getSprite().getName().getPath().equals("blocks/engine/wood/side")).toList();
		trunk_faces = faces.stream().filter((a) -> a.getSprite().getName().getPath().equals("blocks/engine/trunk")).toList();
/*		back_faces.forEach((a) -> a.translated(-1, -1, -1));
		side_faces.forEach((a) -> a.translated(-1, -1, -1));
		trunk_faces.forEach((a) -> a.translated(-1, -1, -1));*/
	}
	
	public static void release() {
		back_faces = null;
		side_faces = null;
		trunk_faces = null;
	}
	
	public ModelEngine(TextureAtlasSprite back, TextureAtlasSprite side) {
		if(back_faces == null) {
			BCLog.logger.error("You should call init method firstly!");
			throw new IllegalStateException("ModelEngine has not benn initalized");
		}
		particleTex = back;
		back_faces.forEach((a) -> a.setSpriteAndUVs(back));
		side_faces.forEach((a) -> a.setSpriteAndUVs(side));
		List<MutableQuad> fs = new ArrayList<>();
		fs.addAll(back_faces.stream().map(MutableQuad::new).toList());
		fs.addAll(side_faces.stream().map(MutableQuad::new).toList());
		fs.addAll(trunk_faces.stream().map(MutableQuad::new).toList());
 		for(Direction d : Direction.values()) {
 			faces.put(d, fs.stream().map((a) -> new MutableQuad(a).rotate(Direction.UP, d, 0.5f, 0.5f, 0.5f).toBakedBlock()).toList());
		}
		
	}
	
	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
			@NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		Direction dir = data.get(EngineModelFacingKey);
		return faces.get(dir == null ? Direction.UP : dir);
//		return BakedModel.super.getQuads(state, side, rand, data, renderType);
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
	public boolean usesBlockLight() {
		return true;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return particleTex;
	}

	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

}
