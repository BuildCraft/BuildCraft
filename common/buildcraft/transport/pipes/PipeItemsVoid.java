/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.DefaultProps;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;

public class PipeItemsVoid extends Pipe implements IItemTravelingHook {

	public PipeItemsVoid(int itemID) {
		super(new PipeTransportItems(), new PipeLogicVoid(), itemID);
		((PipeTransportItems) transport).travelHook = this;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		return 8 * 16 + 14;
	}

	// This is called if the void pipe is only connected to one pipe
	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {
		data.item.getItemStack().stackSize = 0;
	}

	// This is called when the void pipe is connected to multiple pipes
	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {
		((PipeTransportItems) transport).scheduleRemoval(data.item);
	}

	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
	}

}
