/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.pipes.events.PipeEventPriority;

public class PipeItemsClay extends Pipe<PipeTransportItems> {

	public PipeItemsClay(Item item) {
		super(new PipeTransportItems(), item);

		transport.allowBouncing = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeItemsClay.ordinal();
	}

	@PipeEventPriority(priority = -200)
	public void eventHandler(PipeEventItem.FindDest event) {
		LinkedList<ForgeDirection> nonPipesList = new LinkedList<ForgeDirection>();
		LinkedList<ForgeDirection> pipesList = new LinkedList<ForgeDirection>();

		for (ForgeDirection o : event.destinations) {
			if (!event.item.blacklist.contains(o) && container.pipe.outputOpen(o)) {
				if (container.isPipeConnected(o)) {
					TileEntity entity = container.getTile(o);
					if (entity instanceof IPipeTile) {
						pipesList.add(o);
					} else {
						nonPipesList.add(o);
					}
				}
			}
		}

		event.destinations.clear();
		if (nonPipesList.isEmpty()) {
			event.destinations.addAll(pipesList);
		} else {
			event.destinations.addAll(nonPipesList);
		}
	}
}
