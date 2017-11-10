/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.pipes.events.PipeEventFluid;

public class PipeFluidsClay extends Pipe<PipeTransportFluids> {

	public PipeFluidsClay(Item item) {
		super(new PipeTransportFluids(), item);

		transport.initFromPipe(getClass());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeFluidsClay.ordinal();
	}

	public void eventHandler(PipeEventFluid.FindDest event) {
		Set<ForgeDirection> machineDirs = new HashSet<ForgeDirection>();
		Set<ForgeDirection> pipeDirs = new HashSet<ForgeDirection>();

		for (ForgeDirection dir : event.destinations) {
			if (container.isPipeConnected(dir)) {
				TileEntity e = container.getTile(dir);
				if (e instanceof IFluidHandler) {
					IFluidHandler h = (IFluidHandler) e;
					if (h.fill(dir.getOpposite(), event.fluidStack, false) > 0) {
						if (e instanceof IPipeTile) {
							pipeDirs.add(dir);
						} else {
							machineDirs.add(dir);
						}
					}
				}
			}
		}

		event.destinations.clear();
		event.destinations.addAll(machineDirs.size() > 0 ? machineDirs : pipeDirs);
	}
}
