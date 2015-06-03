package buildcraft.core.lib.utils;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.BlockPos;

import buildcraft.api.core.IZone;

public class BlockScannerZoneRandom implements Iterable<BlockPos> {

	private Random rand;
	private IZone zone;
	private int x;
	private int y;
	private int z;

	class BlockIt implements Iterator<BlockPos> {

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public BlockPos next() {
			BlockPos block = zone.getRandomBlockPos(rand);
			return new BlockPos(block.x - x, block.y - y, block.z - z);
		}

		@Override
		public void remove() {
		}
	}

	public BlockScannerZoneRandom(int iX, int iY, int iZ, Random iRand, IZone iZone) {
		x = iX;
		y = iY;
		z = iZ;
		rand = iRand;
		zone = iZone;
	}

	@Override
	public Iterator<BlockPos> iterator() {
		return new BlockIt();
	}

}
