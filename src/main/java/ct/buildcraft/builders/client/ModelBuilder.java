package ct.buildcraft.builders.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.lib.client.model.MutableQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.model.data.ModelData;

public enum ModelBuilder implements BakedModel{
	
	INSTANCE;
	
	public static final ResourceLocation builder = new ModelResourceLocation("buildcraftbuilders:builder");
	
	public static final ResourceLocation empty = new ModelResourceLocation("buildcraftbuilders:block/builder/slot_empty");
	public static final ResourceLocation blueprint = new ModelResourceLocation("buildcraftbuilders:block/builder/slot_blueprint");
	public static final ResourceLocation template = new ModelResourceLocation("buildcraftbuilders:block/builder/slot_template");
	
	public static final List<ModelResourceLocation> stateDefinetion = BCBuildersBlocks.BUILDER.get().getStateDefinition().getPossibleStates().stream().map(BlockModelShaper::stateToModelLocation).toList();
		/*{
			new ModelResourceLocation("buildcraftbuilders:builder#facing=south,snapshot_type=template"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=west,snapshot_type=template"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=south,snapshot_type=blueprint"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=west,snapshot_type=none"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=east,snapshot_type=blueprint"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=north,snapshot_type=blueprint"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=north,snapshot_type=template"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=north,snapshot_type=none"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=east,snapshot_type=none"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=east,snapshot_type=template"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=south,snapshot_type=none"),
			new ModelResourceLocation("buildcraftbuilders:builder#facing=west,snapshot_type=blueprint")
		};;*/
			
	protected static final EnumMap<Direction, List<BakedQuad>> main = new EnumMap<>(Direction.class);
	
	public static void init(BakedModel mainModel) {
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		RandomSource s = RandomSource.create();
		for(Direction  d : Direction.values()) 
			main.put(d, new ArrayList<>(mainModel.getQuads(BCBuildersBlocks.BUILDER.get().defaultBlockState(), d, s, ModelData.EMPTY, null)));
		try {
			resourceManager.openAsReader(new ResourceLocation(builder.getNamespace(), "models/" + builder.getPath() + ".json"));
		}
		catch(IOException e) {
			BCLog.logger.error("Cannot load full model of buildcraftbuilders:block/builder "+e.getMessage());
		}
	}
	
	public static void initPart(BakedModel mainModel) {
		RandomSource s = RandomSource.create();
		main.put(Direction.NORTH, mainModel.getQuads(BCBuildersBlocks.BUILDER.get().defaultBlockState(), Direction.NORTH, s, ModelData.EMPTY, null));
		mainModel.getQuads(BCBuildersBlocks.BUILDER.get().defaultBlockState(), Direction.NORTH, s, ModelData.EMPTY, null).stream()
			.map(MutableQuad::creatByBlock)
			.peek((p) -> main.get(Direction.NORTH).add(p.toBakedBlock()))
			.peek((p) -> main.get(Direction.EAST).add(p.rotateZ_90(1).toBakedBlock()))
			.peek((p) -> main.get(Direction.SOUTH).add(p.rotateZ_90(1).toBakedBlock()))
			.forEach((p) -> main.get(Direction.WEST).add(p.rotateZ_90(1).toBakedBlock()));
			
	}
	
    public static void onModelBakePre(RegisterAdditional event) {
    	event.register(empty);
    	
    }
	
	@Override
	public List<BakedQuad> getQuads(BlockState p_235039_, Direction p_235040_, RandomSource p_235041_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean useAmbientOcclusion() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isGui3d() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return null;
	}

	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

}
