package buildcraft.api.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public abstract class PipeManager {

	@Deprecated
	private static TreeMap<Integer, IPipedItem> allServerEntities = new TreeMap<Integer, IPipedItem>();
	@Deprecated
	private static TreeMap<Integer, IPipedItem> allClientEntities = new TreeMap<Integer, IPipedItem>();

	public static List<IExtractionHandler> extractionHandlers = new ArrayList<IExtractionHandler>();

	public static void registerExtractionHandler(IExtractionHandler handler) {
		extractionHandlers.add(handler);
	}

	public static boolean canExtractItems(IPipe pipe, World world, int i, int j, int k) {
		for (IExtractionHandler handler : extractionHandlers)
			if (!handler.canExtractItems(pipe, world, i, j, k))
				return false;

		return true;
	}

	public static boolean canExtractLiquids(IPipe pipe, World world, int i, int j, int k) {
		for (IExtractionHandler handler : extractionHandlers)
			if (!handler.canExtractLiquids(pipe, world, i, j, k))
				return false;

		return true;
	}

	@Deprecated
	public static TreeMap<Integer, IPipedItem> getAllEntities() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			return allClientEntities;
		return allServerEntities;
	}
}
