package buildcraft.core.robots.boards;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.IRedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.robots.EntityRobot;

public class BoardRobotLumberjack implements IRedstoneBoardRobot<EntityRobot> {

	private NBTTagCompound data;
	private RedstoneBoardNBT board;
	private int range;
	private boolean initialized = false;

	public BoardRobotLumberjack(NBTTagCompound nbt) {
		data = nbt;

		board = RedstoneBoardRegistry.instance.getRedstoneBoard(nbt);
	}

	@Override
	public void updateBoard(EntityRobot robot) {
		if (robot.worldObj.isRemote) {
			return;
		}

		if (!initialized) {
			range = data.getInteger("range");

			initialized = true;
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotLumberjackNBT.instance;
	}
}
