/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.world.World;

public abstract class PipeManager {

	public static final List<IExtractionHandler> extractionHandlers = new ArrayList<IExtractionHandler>();
	public static final Map<Item, IStripesHandler> stripesHandlers = new HashMap<Item, IStripesHandler>();

	public static void registerExtractionHandler(IExtractionHandler handler) {
		extractionHandlers.add(handler);
	}

    /**
     * Registry for custom classes.
     * This handler has a higher priority, so use it carefully.
     *
     * @param handler Handler to register.
     * @param item Item to handle(NBT and meta should be handled by yourself).
     */
    public static void registerStripesHandler(IStripesHandler handler, Item item) {
        stripesHandlers.put(item, handler);
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
