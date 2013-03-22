package buildcraft.builders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
import buildcraft.BuildCraftBuilders;

public class BuilderProxyClient extends BuilderProxy {
	public static Icon fillerFillAllTexture;
	public static Icon fillerClearTexture;
	public static Icon fillerWallsTexture;
	public static Icon fillerStairsTexture;
	public static Icon fillerFlattenTexture;
	public static Icon fillerPyramidTexture;


	public void initializeTextures() {
	    TextureMap terrainMap = Minecraft.getMinecraft().renderEngine.textureMapItems;
	    fillerFillAllTexture = terrainMap.registerIcons("buildcraft:fillerPatterns/fillAll");
	    fillerClearTexture = terrainMap.registerIcons("buildcraft:fillerPatterns/clear");
	    fillerWallsTexture = terrainMap.registerIcons("buildcraft:fillerPatterns/walls");
	    fillerStairsTexture = terrainMap.registerIcons("buildcraft:fillerPatterns/stairs");
	    fillerFlattenTexture = terrainMap.registerIcons("buildcraft:fillerPatterns/flatten");
	    fillerPyramidTexture = terrainMap.registerIcons("buildcraft:fillerPatterns/pyramid");
	}
    @Override
	public void registerClientHook() {
		BuildCraftBuilders.addHook(new ClientBuilderHook());
	}
}
