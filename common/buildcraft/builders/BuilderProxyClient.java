package buildcraft.builders;

import net.minecraft.util.Icon;
import buildcraft.BuildCraftBuilders;

public class BuilderProxyClient extends BuilderProxy {
	public static Icon fillerFillAllTexture;
	public static Icon fillerClearTexture;
	public static Icon fillerWallsTexture;
	public static Icon fillerStairsTexture;
	public static Icon fillerFlattenTexture;
	public static Icon fillerPyramidTexture;

    @Override
	public void registerClientHook() {
		BuildCraftBuilders.addHook(new ClientBuilderHook());
	}
}
