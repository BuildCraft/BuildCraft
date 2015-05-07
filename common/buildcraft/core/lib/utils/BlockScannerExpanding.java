package buildcraft.core.lib.utils;

import java.util.Iterator;

import buildcraft.api.core.BlockIndex;

public class BlockScannerExpanding implements Iterable<BlockIndex> {

	private int searchRadius;
	private int searchX;
	private int searchY;
	private int searchZ;

	class BlockIt implements Iterator<BlockIndex> {

		@Override
		public boolean hasNext() {
			return searchRadius < 64;
		}

		@Override
		public BlockIndex next() {
			// Step through each block in a hollow cube of size (searchRadius * 2 -1), if done
			// add 1 to the radius and start over.

			BlockIndex next = new BlockIndex(searchX, searchY, searchZ);

			// Step to the next Y
			if (Math.abs(searchX) == searchRadius || Math.abs(searchZ) == searchRadius) {
				searchY += 1;
			} else {
				searchY += searchRadius * 2;
			}

			if (searchY > searchRadius) {
				// Step to the next Z
				searchY = -searchRadius;
				searchZ += 1;

				if (searchZ > searchRadius) {
					// Step to the next X
					searchZ = -searchRadius;
					searchX += 1;

					if (searchX > searchRadius) {
						// Step to the next radius
						searchRadius += 1;
						searchX = -searchRadius;
						searchY = -searchRadius;
						searchZ = -searchRadius;
					}
				}
			}
			return next;
		}

		@Override
		public void remove() {
		}
	}

	public BlockScannerExpanding() {
		searchRadius = 1;
		searchX = -1;
		searchY = -1;
		searchZ = -1;
	}

	@Override
	public Iterator<BlockIndex> iterator() {
		return new BlockIt();
	}

}
