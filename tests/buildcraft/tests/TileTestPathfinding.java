package buildcraft.tests;

import java.util.LinkedList;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.core.BlockPos;
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
				PathFinding p = new PathFinding(worldObj, new BlockPos(xCoord, yCoord, zCoord), new BlockPos(
						firstEntity.xCoord, firstEntity.yCoord, firstEntity.zCoord));

				p.iterate(10000);

				LinkedList<BlockPos> r = p.getResult();

				for (BlockPos b : r) {
					worldObj.setBlock(b.x, b.y, b.z, Blocks.sponge);
				}

				firstEntity = null;
			}
		}
	}
}
