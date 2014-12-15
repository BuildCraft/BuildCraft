/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.World;
import buildcraft.api.transport.IExtractionHandler;

public abstract class PipeManager {

	public static List<IStripesHandler> stripesHandlers = new ArrayList<IStripesHandler>();
	public static List<IExtractionHandler> extractionHandlers = new ArrayList<IExtractionHandler>();
	public static ArrayList<Class<? extends IPipePluggable>> pipePluggables = new ArrayList<Class<? extends IPipePluggable>>();
	private static Map<String, Class<? extends IPipePluggable>> pipePluggableNames =
			new HashMap<String, Class<? extends IPipePluggable>>();
	private static Map<Class<? extends IPipePluggable>, String> pipePluggableByNames =
			new HashMap<Class<? extends IPipePluggable>, String>();

	public static void registerExtractionHandler(IExtractionHandler handler) {
		extractionHandlers.add(handler);
	}
	
	public static void registerStripesHandler(IStripesHandler handler) {
		stripesHandlers.add(handler);
	}

	public static void registerPipePluggable(Class<? extends IPipePluggable> pluggable, String name) {
		pipePluggables.add(pluggable);
		pipePluggableNames.put(name, pluggable);
		pipePluggableByNames.put(pluggable, name);
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

	public static Class<?> getPluggableByName(String pluggableName) {
		return pipePluggableNames.get(pluggableName);
	}

	public static String getPluggableName(Class<? extends IPipePluggable> aClass) {
		return pipePluggableByNames.get(aClass);
	}
}
