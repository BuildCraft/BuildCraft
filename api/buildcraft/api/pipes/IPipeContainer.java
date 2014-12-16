/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.pipes;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.EnumColor;
import buildcraft.api.transport.IInjectable;

public interface IPipeContainer extends IInjectable, IPipePluggableContainer {

	public enum PipeType {

		ITEM, FLUID, POWER, STRUCTURE
	}

	PipeType getPipeType();

	World getWorldObj();

	int x();

	int y();

	int z();

	/**
	 * True if the pipe is connected to the block/pipe in the specific direction
	 * 
	 * @param with
	 * @return true if connect
	 */
	boolean isPipeConnected(ForgeDirection with);

	Block getNeighborBlock(ForgeDirection dir);
	TileEntity getNeighborTile(ForgeDirection dir);
	IPipe getNeighborPipe(ForgeDirection dir);
	
	IPipe getPipe();
}
