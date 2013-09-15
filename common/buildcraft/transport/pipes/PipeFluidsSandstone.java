/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.IPipeConnectionForced;
import buildcraft.transport.IPipeTransportFluidsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeFluidsSandstone extends Pipe<PipeTransportFluids> implements IPipeTransportFluidsHook, IPipeConnectionForced {

	public PipeFluidsSandstone(int itemID) {
		super(new PipeTransportFluids(), itemID);
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
		if (!(container.getTile(from) instanceof TileGenericPipe))
			return 0;

		return transport.internalTanks[from.ordinal()].fill(resource, doFill);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return (tile instanceof TileGenericPipe) && super.canPipeConnect(tile, side);
	}

	@Override
	public boolean ignoreConnectionOverrides(ForgeDirection with) {
		return true;
	}
}
