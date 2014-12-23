/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.BuildCraftTransport;
import buildcraft.transport.IPipeConnectionForced;
import buildcraft.transport.IPipeTransportFluidsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;

public class PipeFluidsSandstone extends Pipe<PipeTransportFluids> implements IPipeTransportFluidsHook, IPipeConnectionForced {

	public PipeFluidsSandstone(Item item) {
		super(new PipeTransportFluids(), item);

        transport.initFromPipe(getClass());
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}*/

	@Override
	public int getIconIndex(EnumFacing direction) {
		return PipeIconProvider.TYPE.PipeFluidsSandstone.ordinal();
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
		if (!(container.getTile(from) instanceof TileGenericPipe)) {
			return 0;
		} else {
			return transport.internalTanks[from.ordinal()].fill(resource, doFill);
		}
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
		return (tile instanceof TileGenericPipe) && super.canPipeConnect(tile, side);
	}

	@Override
	public boolean ignoreConnectionOverrides(EnumFacing with) {
		return true;
	}
}
