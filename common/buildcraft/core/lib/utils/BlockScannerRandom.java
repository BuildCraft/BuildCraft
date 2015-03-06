package buildcraft.core.lib.utils;

import java.util.Iterator;
import java.util.Random;

import buildcraft.api.core.BlockIndex;

public class BlockScannerRandom implements Iterable<BlockIndex> {

	private Random rand;
	private int maxDistance;

	class BlockIt implements Iterator<BlockIndex> {

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public BlockIndex next() {
			double radius = rand.nextFloat() * maxDistance;
			double polarAngle = rand.nextFloat() * 2.0 * Math.PI;
			double azimuthAngle = rand.nextFloat() * Math.PI;

			int searchX = (int) (radius * Math.cos(polarAngle) * Math.sin(azimuthAngle));
			int searchY = (int) (radius * Math.cos(azimuthAngle));
			int searchZ = (int) (radius * Math.sin(polarAngle) * Math.sin(azimuthAngle));

			return new BlockIndex(searchX, searchY, searchZ);
		}

		@Override
		public void remove() {
		}
	}

	public BlockScannerRandom(Random iRand, int iMaxDistance) {
		rand = iRand;
		maxDistance = iMaxDistance;
	}

	@Override
	public Iterator<BlockIndex> iterator() {
		return new BlockIt();
	}

}
