/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

public abstract class PipeManager {

	public static List<IStripesHandler> stripesHandlers = new ArrayList<IStripesHandler>();
	public static List<IExtractionHandler> extractionHandlers = new ArrayList<IExtractionHandler>();

	public static void registerExtractionHandler(IExtractionHandler handler) {
		extractionHandlers.add(handler);
	}
	
	public static void registerStripesHandler(IStripesHandler handler) {
		stripesHandlers.add(handler);
	}

	/**
	 * param extractor can be null
	 */
	public static boolean canExtractItems(Object extractor, World world, int i, int j, int k) {
		for (IExtractionHandler handler : extractionHandlers) {
			if (!handler.canExtractItems(extractor, world, i, j, k)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * param extractor can be null
	 */
	public static boolean canExtractFluids(Object extractor, World world, int i, int j, int k) {
		for (IExtractionHandler handler : extractionHandlers) {
			if (!handler.canExtractFluids(extractor, world, i, j, k)) {
				return false;
			}
		}

		return true;
	}
}
