/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsVoid extends Pipe<PipeTransportItems> implements IItemTravelingHook {

	public PipeItemsVoid(int itemID) {
		super(new PipeTransportItems(), itemID);
		transport.travelHook = this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeItemsVoid.ordinal();
	}

	// This is called if the void pipe is only connected to one pipe
	@Override
	public void drop(PipeTransportItems pipe, TravelingItem item) {
		item.getItemStack().stackSize = 0;
	}

	// This is called when the void pipe is connected to multiple pipes
	@Override
	public void centerReached(PipeTransportItems pipe, TravelingItem item) {
		transport.items.scheduleRemoval(item);
	}

	@Override
	public boolean endReached(PipeTransportItems pipe, TravelingItem item, TileEntity tile) {
		return false;
	}
}
