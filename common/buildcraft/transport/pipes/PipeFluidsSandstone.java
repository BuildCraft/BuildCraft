/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.IPipeTransportFluidsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class PipeFluidsSandstone extends Pipe implements IPipeTransportFluidsHook {
	public PipeFluidsSandstone(int itemID) {
		super(new PipeTransportFluids(), new PipeLogicSandstone(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeFluidsSandstone.ordinal();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (container.tileBuffer == null || container.tileBuffer[from.ordinal()] == null)
			return 0;

		if (!(container.tileBuffer[from.ordinal()].getTile() instanceof IPipeTile))
			return 0;

		return ((PipeTransportFluids) this.transport).fill(from, resource, doFill);
	}
}
