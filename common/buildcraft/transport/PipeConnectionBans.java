/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.transport.pipes.PipeFluidsCobblestone;
import buildcraft.transport.pipes.PipeFluidsStone;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsEmzuli;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeItemsQuartz;
import buildcraft.transport.pipes.PipeItemsStone;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipePowerWood;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Controls whether one type of pipe can connect to another.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class PipeConnectionBans {

	private static final SetMultimap<Class<? extends Pipe>, Class<? extends Pipe>> connectionBans = HashMultimap.create();

	static {
		// Fluid pipes
		banConnection(PipeFluidsStone.class, PipeFluidsCobblestone.class);

		banConnection(PipeFluidsWood.class);

		// Item Pipes		
		banConnection(PipeItemsStone.class, PipeItemsCobblestone.class, PipeItemsQuartz.class);

		banConnection(PipeItemsWood.class);

		banConnection(PipeItemsObsidian.class);
		
		banConnection(PipeItemsEmzuli.class);

		// Power Pipes
		banConnection(PipePowerWood.class);
	}

	private PipeConnectionBans() {
	}

	/**
	 * Will ban connection between any set of pipe types provided.
	 *
	 * If only one parameter is passed in, it will ban connection to pipes of
	 * the same type.
	 *
	 * @param types
	 */
	public static void banConnection(Class<? extends Pipe>... types) {
		if (types.length == 0)
			return;
		if (types.length == 1) {
			connectionBans.put(types[0], types[0]);
			return;
		}
		for (int i = 0; i < types.length; i++) {
			for (int j = 0; j < types.length; j++) {
				if (i == j)
					continue;
				connectionBans.put(types[i], types[j]);
			}
		}
	}

	public static boolean canPipesConnect(Class<? extends Pipe> type1, Class<? extends Pipe> type2) {
		return !connectionBans.containsEntry(type1, type2);
	}
}
