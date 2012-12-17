/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.core.DefaultProps;
import buildcraft.transport.IPipeTransportLiquidsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsVoid extends Pipe implements IPipeTransportLiquidsHook {

	public PipeLiquidsVoid(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicVoid(), itemID);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		return 9 * 16 + 14;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		return resource.amount;
	}
}
