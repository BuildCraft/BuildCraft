package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGotoSleep;

public class BoardRobotEmpty extends RedstoneBoardRobot {

	public BoardRobotEmpty(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return RedstoneBoardRobotEmptyNBT.instance;
	}

	@Override
	public void update() {
		startDelegateAI(new AIRobotGotoSleep(robot));
	}
}
