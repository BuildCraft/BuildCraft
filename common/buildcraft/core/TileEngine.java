package buildcraft.core;

import net.minecraft.util.ResourceLocation;
import buildcraft.core.lib.engines.TileEngineBase;

/**
 * Created by asie on 3/6/15.
 */
public abstract class TileEngine extends TileEngineBase {
	// Index corresponds to metadata
	public static final ResourceLocation[] BASE_TEXTURES = new ResourceLocation[]{
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_stone.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_iron.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_creative.png")
	};

	public static final ResourceLocation[] CHAMBER_TEXTURES = new ResourceLocation[]{
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_wood.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_stone.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_iron.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_creative.png")
	};

	// THESE ARE ONLY BLUE TRUNKS. OTHER HEAT STAGES ARE HANDLED PER TILE
	public static final ResourceLocation[] TRUNK_TEXTURES = new ResourceLocation[]{
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_blue.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_blue.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_blue.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_creative.png")
	};
}
