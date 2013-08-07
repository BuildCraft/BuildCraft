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
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.LinkedList;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsQuartz extends Pipe implements IPipeTransportItemsHook {

	public PipeItemsQuartz(int itemID) {
		super(new PipeTransportItems(), itemID);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeItemsQuartz.ordinal();
	}

	@Override
	public void readjustSpeed(TravelingItem item) {
		if (item.getSpeed() > Utils.pipeNormalSpeed) {
			item.setSpeed(item.getSpeed() - Utils.pipeNormalSpeed / 4.0F);
		}

		if (item.getSpeed() < Utils.pipeNormalSpeed) {
			item.setSpeed(Utils.pipeNormalSpeed);
		}
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(TravelingItem item, ForgeDirection orientation) {
	}
}
