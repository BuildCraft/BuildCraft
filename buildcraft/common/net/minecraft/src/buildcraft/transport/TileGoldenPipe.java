package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.Utils;

public class TileGoldenPipe extends TilePipe {

	public void entityEntering (EntityPassiveItem item, Orientations orientation) {		
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			item.speed = Utils.pipeNormalSpeed * 20F;
		}
		
		super.entityEntering(item, orientation);
	}

}
