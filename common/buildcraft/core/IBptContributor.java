/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.core.blueprints.BptBase;
import net.minecraft.tileentity.TileEntity;

public interface IBptContributor {

	public void saveToBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

	public void loadFromBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

}
