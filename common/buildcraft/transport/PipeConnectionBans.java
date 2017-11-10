/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import buildcraft.transport.pipes.PipeFluidsCobblestone;
import buildcraft.transport.pipes.PipeFluidsEmerald;
import buildcraft.transport.pipes.PipeFluidsQuartz;
import buildcraft.transport.pipes.PipeFluidsStone;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsEmzuli;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeItemsQuartz;
import buildcraft.transport.pipes.PipeItemsStone;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipePowerEmerald;
import buildcraft.transport.pipes.PipePowerWood;

/**
 * Controls whether one type of pipe can connect to another.
 */
public final class PipeConnectionBans {

	private static final SetMultimap<Class<? extends Pipe<?>>, Class<? extends Pipe<?>>> connectionBans = HashMultimap.create();

	static {
		// Fluid pipes
		banConnection(PipeFluidsStone.class, PipeFluidsCobblestone.class, PipeFluidsQuartz.class);
		banConnection(PipeFluidsWood.class);
		banConnection(PipeFluidsEmerald.class);
		banConnection(PipeFluidsWood.class, PipeFluidsEmerald.class);

		// Item Pipes
		banConnection(PipeItemsStone.class, PipeItemsCobblestone.class, PipeItemsQuartz.class);
		banConnection(PipeItemsWood.class);
		banConnection(PipeItemsEmerald.class);
		banConnection(PipeItemsEmzuli.class);
		banConnection(PipeItemsWood.class, PipeItemsEmerald.class, PipeItemsEmzuli.class);
		banConnection(PipeItemsObsidian.class);

		// Power Pipes
		banConnection(PipePowerWood.class);
		banConnection(PipePowerEmerald.class);
		banConnection(PipePowerWood.class, PipePowerEmerald.class);
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
	public static void banConnection(Class<? extends Pipe<?>>... types) {
		if (types.length == 0) {
			return;
		}
		if (types.length == 1) {
			connectionBans.put(types[0], types[0]);
			return;
		}
		for (int i = 0; i < types.length; i++) {
			for (int j = 0; j < types.length; j++) {
				if (i == j) {
					continue;
				}
				connectionBans.put(types[i], types[j]);
			}
		}
	}

	public static boolean canPipesConnect(Class<? extends Pipe> type1, Class<? extends Pipe> type2) {
		return !connectionBans.containsEntry(type1, type2);
	}
}
