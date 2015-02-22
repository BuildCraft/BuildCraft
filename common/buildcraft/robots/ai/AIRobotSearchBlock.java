/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.BlockScannerExpanding;
import buildcraft.core.utils.IBlockFilter;

public class AIRobotSearchBlock extends AIRobotSearchBlockBase {

	public AIRobotSearchBlock(EntityRobotBase iRobot, IBlockFilter iPathFound) {
		super(iRobot, iPathFound, new BlockScannerExpanding().iterator());
	}

}
