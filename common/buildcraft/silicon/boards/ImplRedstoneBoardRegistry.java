/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.boards;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.IRedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;

public class ImplRedstoneBoardRegistry extends RedstoneBoardRegistry {

	private static class BoardFactory {
		public IRedstoneBoardNBT boardNBT;
		public float probability;
	}

	private float totalProbability;

	private HashMap<String, BoardFactory> boards = new HashMap<String, BoardFactory>();

	private Random rand = new Random();

	@Override
	public void registerBoardClass(IRedstoneBoardNBT redstoneBoardNBT, float probability) {
		BoardFactory factory = new BoardFactory();
		factory.boardNBT = redstoneBoardNBT;
		factory.probability = probability;

		totalProbability += probability;
		boards.put(redstoneBoardNBT.getID(), factory);
	}

	@Override
	public void createRandomBoard(NBTTagCompound nbt) {
		float value = rand.nextFloat() * totalProbability;

		float accumulatedSearch = 0;

		for (BoardFactory f : boards.values()) {
			accumulatedSearch += f.probability;

			if (accumulatedSearch < value) {
				nbt.setString("id", f.boardNBT.getID());
				return;
			}
		}
	}

	@Override
	public IRedstoneBoardNBT getRedstoneBoard(NBTTagCompound nbt) {
		return boards.get(nbt.getString("id")).boardNBT;
	}
}
