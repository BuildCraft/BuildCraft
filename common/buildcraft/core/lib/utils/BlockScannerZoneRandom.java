package buildcraft.core.lib.utils;

import java.util.Iterator;
import java.util.Random;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IZone;

public class BlockScannerZoneRandom implements Iterable<BlockIndex> {

	private Random rand;
	private IZone zone;
	private int x;
	private int y;
	private int z;

	class BlockIt implements Iterator<BlockIndex> {

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public BlockIndex next() {
			BlockIndex block = zone.getRandomBlockIndex(rand);
			return new BlockIndex(block.x - x, block.y - y, block.z - z);
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
	public Iterator<BlockIndex> iterator() {
		return new BlockIt();
	}

}
