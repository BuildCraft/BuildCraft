package net.minecraft.src.buildcraft;

public class TileGoldenPipe extends TileStonePipe {

	public void entityEntering (EntityPassiveItem item, Orientations orientation) {
		super.entityEntering(item, orientation);
		
		if (world.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			item.speed = Utils.pipeNormalSpeed * 20F;
		}
	}

}
