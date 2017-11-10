/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;

public class ImplRedstoneBoardRegistry extends RedstoneBoardRegistry {
	private static class BoardFactory {
		public RedstoneBoardNBT<?> boardNBT;
		public int energyCost;
	}

	private HashMap<String, BoardFactory> boards = new HashMap<String, BoardFactory>();
	private RedstoneBoardRobotNBT emptyRobotBoardNBT;

	@Override
	public void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, int energyCost) {
		if (BuildCraftRobotics.blacklistedRobots.contains(redstoneBoardNBT.getID())) {
			return;
		}

		BoardFactory factory = new BoardFactory();
		factory.boardNBT = redstoneBoardNBT;
		factory.energyCost = energyCost;

		boards.put(redstoneBoardNBT.getID(), factory);
	}

	@Override
	public void registerBoardClass(RedstoneBoardNBT<?> redstoneBoardNBT, float probability) {
		this.registerBoardType(redstoneBoardNBT, Math.round(160000 / probability));
	}

	@Override
	public void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT) {
		emptyRobotBoardNBT = redstoneBoardNBT;
	}

	@Override
	public RedstoneBoardRobotNBT getEmptyRobotBoard() {
		return emptyRobotBoardNBT;
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
			return emptyRobotBoardNBT;
		}
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		emptyRobotBoardNBT.registerIcons(par1IconRegister);
		for (BoardFactory f : boards.values()) {
			f.boardNBT.registerIcons(par1IconRegister);
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

	@Override
	public int getEnergyCost(RedstoneBoardNBT<?> board) {
		return boards.get(board.getID()).energyCost;
	}

}
