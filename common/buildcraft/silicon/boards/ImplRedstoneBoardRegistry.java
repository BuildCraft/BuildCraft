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
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.IBoardParameter;
import buildcraft.api.boards.IBoardParameterStack;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;

public class ImplRedstoneBoardRegistry extends RedstoneBoardRegistry {

	private static class BoardFactory {
		public RedstoneBoardNBT<?> boardNBT;
		public float probability;
	}

	private float totalProbability;

	private HashMap<String, BoardFactory> boards = new HashMap<String, BoardFactory>();

	private Random rand = new Random();

	@Override
	public void registerBoardClass(RedstoneBoardNBT<?> redstoneBoardNBT, float probability) {
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

			if (accumulatedSearch > value) {
				nbt.setString("id", f.boardNBT.getID());
				f.boardNBT.createRandomBoard(nbt);
				return;
			}
		}
	}

	@Override
	public RedstoneBoardNBT<?> getRedstoneBoard(NBTTagCompound nbt) {
		return getRedstoneBoard(nbt.getString("id"));
	}

	@Override
	public RedstoneBoardNBT<?> getRedstoneBoard(String id) {
		BoardFactory factory = boards.get(id);

		if (factory != null) {
			return factory.boardNBT;
		} else {
			return null;
		}
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		for (BoardFactory f : boards.values()) {
			f.boardNBT.registerIcons(par1IconRegister);
		}
	}

	@Override
	public IBoardParameterStack createParameterStack() {
		return new BoardParameterStack();
	}

	@Override
	public IBoardParameterStack createParameter(String kind) {
		if ("stack".equals(kind)) {
			return createParameterStack();
		} else {
			return null;
		}
	}

	@Override
	public String getKindForParam(IBoardParameter param) {
		if (param instanceof BoardParameterStack) {
			return "stack";
		} else {
			return null;
		}
	}

	@Override
	public Collection<RedstoneBoardNBT<?>> getAllBoardNBTs() {
		ArrayList<RedstoneBoardNBT<?>> result = new ArrayList<RedstoneBoardNBT<?>>();

		for (BoardFactory f : boards.values()) {
			result.add(f.boardNBT);
		}

		return result;
	}
}
