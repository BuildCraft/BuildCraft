package net.minecraft.src.buildcraft.transport.pipes;

import java.util.LinkedList;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.transport.IPipeTransportItemsHook;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicDiamond;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsDiamond extends Pipe implements IPipeTransportItemsHook {

	int nextTexture = 1 * 16 + 5;
	
	public PipeItemsDiamond(int itemID) {
		super(new PipeTransportItems(), new PipeLogicDiamond(), itemID);
	}

	
	public int getBlockTexture() {
		return nextTexture;
	}
	
	public void prepareTextureFor(Orientations connection) {
		if (connection == Orientations.Unknown) {
			nextTexture = 1 * 16 + 5;
		} else {
			nextTexture = BuildCraftTransport.diamondTextures[connection.ordinal()];
		}
	}
	
	@Override
	public LinkedList<Orientations> filterPossibleMovements(LinkedList<Orientations> possibleOrientations, Position pos,
			EntityPassiveItem item) {
		LinkedList<Orientations> filteredOrientations = new LinkedList<Orientations>();
		LinkedList<Orientations> defaultOrientations = new LinkedList<Orientations>();
								
		//Filtered outputs
		for (Orientations dir : possibleOrientations) {
			boolean foundFilter = false;

			// NB: if there's several of the same match, the probability
			// to use that filter is higher, this is why there are
			// no breaks here.
			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = logic.getStackInSlot(dir.ordinal() * 9 + slot);

				if (stack != null) {
					foundFilter = true;
				}

				if (stack != null && stack.itemID == item.item.itemID)
				{
					if ((Item.itemsList[item.item.itemID].isDamageable())) {
						filteredOrientations.add(dir);
					} else if (stack.getItemDamage() == item.item
							.getItemDamage()) {
						filteredOrientations.add(dir);
					}
				} 
			}
			if (!foundFilter) {				
				defaultOrientations.add(dir);
			}
		}
		if(filteredOrientations.size() != 0) {
			return filteredOrientations;
		} else {
			return defaultOrientations;
		}
	}
	
	
}
