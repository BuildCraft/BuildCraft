/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.boards;

import java.util.ArrayList;
import java.util.Random;

import buildcraft.api.boards.IRedstoneBoard;
import buildcraft.api.boards.RedstoneBoardRegistry;

public class ImplRedstoneBoardRegistry extends RedstoneBoardRegistry {

	private static class BoardFactory {
		public Class<? extends IRedstoneBoard> clas;
		public float probability;
	}

	private float totalProbability;

	private ArrayList<BoardFactory> boards = new ArrayList<BoardFactory>();

	private Random rand = new Random();

	@Override
	public void registerBoardClass(Class<? extends IRedstoneBoard> boardClass, float probability) {
		BoardFactory factory = new BoardFactory();
		factory.clas = boardClass;
		factory.probability = probability;

		totalProbability += probability;
		boards.add(factory);
	}

	@Override
	public IRedstoneBoard createRandomBoard() {
		float value = rand.nextFloat() * totalProbability;

		float accumulatedSearch = 0;

		for (BoardFactory f : boards) {
			accumulatedSearch += f.probability;

			if (accumulatedSearch < value) {
				try {
					return f.clas.newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
