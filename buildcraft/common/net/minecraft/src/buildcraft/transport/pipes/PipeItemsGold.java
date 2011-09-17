package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicGold;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsGold extends Pipe {

	public PipeItemsGold(int itemID) {
		super(new PipeTransportItems(), new PipeLogicGold(), itemID);
	}

	public int getBlockTexture() {
		if (worldObj != null
				&& worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord,
						zCoord)) {
			return 1 * 16 + 14;
		} else {
			return 1 * 16 + 4;
		}
	}
}
