/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import java.util.Iterator;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.utils.BlockScannerRandom;
import buildcraft.core.utils.BlockScannerZoneRandom;
import buildcraft.core.utils.IBlockFilter;

public class AIRobotSearchRandomBlock extends AIRobotSearchBlockBase {

	public AIRobotSearchRandomBlock(EntityRobotBase iRobot, IBlockFilter iPathFound) {
		super(iRobot, iPathFound, getBlockIterator(iRobot));
	}

	private static Iterator<BlockIndex> getBlockIterator(EntityRobotBase iRobot) {
		IZone zone = iRobot.getZoneToWork();
		if (zone != null) {
			BlockIndex pos = new BlockIndex(iRobot);
			return new BlockScannerZoneRandom(pos.x, pos.y, pos.z, iRobot.worldObj.rand, zone).iterator();
		} else {
			return new BlockScannerRandom(iRobot.worldObj.rand, 64).iterator();
		}
	}

}
