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
	    fillerFillAllTexture = terrainMap.registerIcon("buildcraft:fillerPatterns/fillAll");
	    fillerClearTexture = terrainMap.registerIcon("buildcraft:fillerPatterns/clear");
	    fillerWallsTexture = terrainMap.registerIcon("buildcraft:fillerPatterns/walls");
	    fillerStairsTexture = terrainMap.registerIcon("buildcraft:fillerPatterns/stairs");
	    fillerFlattenTexture = terrainMap.registerIcon("buildcraft:fillerPatterns/flatten");
	    fillerPyramidTexture = terrainMap.registerIcon("buildcraft:fillerPatterns/pyramid");
	}
    @Override
	public void registerClientHook() {
		BuildCraftBuilders.addHook(new ClientBuilderHook());
	}
}
