/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelingItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.LinkedList;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsGold extends Pipe<PipeTransportItems> implements IPipeTransportItemsHook {

	public PipeItemsGold(int itemID) {
		super(new PipeTransportItems(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeItemsGold.ordinal();
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(TravelingItem item, ForgeDirection orientation) {
		readjustSpeed(item);
	}

	@Override
	public void readjustSpeed(TravelingItem item) {
		item.setSpeed(Math.min(Math.max(TransportConstants.PIPE_NORMAL_SPEED, item.getSpeed()) * 2f, TransportConstants.PIPE_NORMAL_SPEED * 20F));
	}
}
