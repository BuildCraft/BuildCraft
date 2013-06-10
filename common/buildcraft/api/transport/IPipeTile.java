/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.transport;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public interface IPipeTile {

	IPipe getPipe();

	boolean isInitialized();

	TileEntity getTile(ForgeDirection o);
	
	int getXCoord();
	int getYCoord();
	int getZCoord();

	void scheduleRenderUpdate();

	World getWorld();
}
