package buildcraft.lib.misc;

import net.minecraftforge.fml.common.Loader;

public class CubicChunksChecker {

	/** The Y height level that is the lowest in the world */
	public static int worldMin = 0;

	public static void postInit()
	{
		if(Loader.isModLoaded("cubicchunks")) {
			worldMin = -1073741824;
		}
	}
}