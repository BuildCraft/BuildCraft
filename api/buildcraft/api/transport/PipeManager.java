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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.World;

import buildcraft.api.transport.pluggable.PipePluggable;

public abstract class PipeManager {

	public static List<IStripesHandler> stripesHandlers = new ArrayList<IStripesHandler>();
	public static ArrayList<Class<? extends PipePluggable>> pipePluggables = new ArrayList<Class<? extends PipePluggable>>();
	private static Map<String, Class<? extends PipePluggable>> pipePluggableNames =
			new HashMap<String, Class<? extends PipePluggable>>();
	private static Map<Class<? extends PipePluggable>, String> pipePluggableByNames =
			new HashMap<Class<? extends PipePluggable>, String>();

	@Deprecated
	public static boolean canExtractItems(Object extractor, World world, int i, int j, int k) {
		return true;
	}

	@Deprecated
	public static boolean canExtractFluids(Object extractor, World world, int i, int j, int k) {
		return true;
	}

	public static void registerStripesHandler(IStripesHandler handler) {
		stripesHandlers.add(handler);
	}

	public static void registerPipePluggable(Class<? extends PipePluggable> pluggable, String name) {
		pipePluggables.add(pluggable);
		pipePluggableNames.put(name, pluggable);
		pipePluggableByNames.put(pluggable, name);
	}

	public static Class<?> getPluggableByName(String pluggableName) {
		return pipePluggableNames.get(pluggableName);
	}

	public static String getPluggableName(Class<? extends PipePluggable> aClass) {
		return pipePluggableByNames.get(aClass);
	}
}
