package buildcraft.transport.pipes;

import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.PipeTransportItems;

public class PipeItemsInsertion extends Pipe implements IPipeTransportItemsHook {

	public PipeItemsInsertion(int itemID) {
		super(new PipeTransportItems(), new PipeLogicStone(), itemID);
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, IPipedItem item) {
		LinkedList<ForgeDirection> inventories = new LinkedList<ForgeDirection>();
		
		for(ForgeDirection dir : possibleOrientations) {
			if(getContainer().getTile(dir) instanceof IInventory) {
				inventories.add(dir);
			}
		}
		
		if(inventories.isEmpty())
			return possibleOrientations;
		else
			return inventories;
	}

	@Override
	public void entityEntered(IPipedItem item, ForgeDirection orientation) {
		
	}

	@Override
	public void readjustSpeed(IPipedItem item) {
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.PipeItemsInsertion;
	}

}
