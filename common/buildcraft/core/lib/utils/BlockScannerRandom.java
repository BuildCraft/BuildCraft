package buildcraft.core.lib.utils;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.MathHelper;

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
			float polarAngle = rand.nextFloat() * 2.0F * (float) Math.PI;
			float azimuthAngle = rand.nextFloat() * (float) Math.PI;

			int searchX = (int) (radius * MathHelper.cos(polarAngle) * MathHelper.sin(azimuthAngle));
			int searchY = (int) (radius * MathHelper.cos(azimuthAngle));
			int searchZ = (int) (radius * MathHelper.sin(polarAngle) * MathHelper.sin(azimuthAngle));

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
