package buildcraft.tests;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

import buildcraft.core.BlockIndex;
import buildcraft.core.utils.PathFinding;

public class TileTestPathfinding extends TileEntity {

	static TileTestPathfinding firstEntity = null;

	public boolean initialized = false;

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) {
			return;
		}

		if (!initialized) {
			initialized = true;

			if (firstEntity == null) {
				firstEntity = this;
			} else {
				PathFinding p = new PathFinding(worldObj, new BlockIndex(xCoord, yCoord, zCoord), new BlockIndex(
						firstEntity.xCoord, firstEntity.yCoord, firstEntity.zCoord));

				p.iterate(100);

				ArrayList<BlockIndex> r = p.getResult();

				for (BlockIndex b : r) {
					worldObj.setBlock(b.x, b.y, b.z, Blocks.sponge);
				}
			}
		}
	}

}
