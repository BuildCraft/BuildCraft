package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.Utils;

public class TileGoldenPipe extends TileStonePipe {

	public void entityEntering (EntityPassiveItem item, Orientations orientation) {
		super.entityEntering(item, orientation);
		
		if (world.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			item.speed = Utils.pipeNormalSpeed * 20F;
		}
	}

}
