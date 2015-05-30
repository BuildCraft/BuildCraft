package buildcraft.core.utils;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public final class TextureMapHelper {

	private TextureMapHelper() {}

	public static TextureAtlasSprite registerSprite(TextureMap map, String location) {
		return registerSprite(map, new ResourceLocation(location));
	}

	public static TextureAtlasSprite registerSprite(TextureMap map, ResourceLocation location) {
		TextureAtlasSprite atlasSprite = map.registerSprite(location);
		map.setTextureEntry(location.toString(), atlasSprite);
		return atlasSprite;
	}
}
