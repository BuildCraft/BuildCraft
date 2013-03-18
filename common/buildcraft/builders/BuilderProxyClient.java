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
	    TextureMap terrainMap = Minecraft.getMinecraft().renderEngine.field_94154_l;
	    fillerFillAllTexture = terrainMap.func_94245_a("buildcraft:fillerPatterns/fillAll");
	    fillerClearTexture = terrainMap.func_94245_a("buildcraft:fillerPatterns/clear");
	    fillerWallsTexture = terrainMap.func_94245_a("buildcraft:fillerPatterns/walls");
	    fillerStairsTexture = terrainMap.func_94245_a("buildcraft:fillerPatterns/stairs");
	    fillerFlattenTexture = terrainMap.func_94245_a("buildcraft:fillerPatterns/flatten");
	    fillerPyramidTexture = terrainMap.func_94245_a("buildcraft:fillerPatterns/pyramid");
	}
    @Override
	public void registerClientHook() {
		BuildCraftBuilders.addHook(new ClientBuilderHook());
	}
}
