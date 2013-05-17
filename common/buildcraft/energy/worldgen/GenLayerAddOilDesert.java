package buildcraft.energy.worldgen;

import buildcraft.BuildCraftEnergy;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class GenLayerAddOilDesert extends GenLayer {

	public GenLayerAddOilDesert(final long size, final GenLayer genLayer) {
		super(size);
		parent = genLayer;
	}

	@Override
	public int[] getInts(final int x, final int y, final int width, final int length) {
		final int[] inputBiomeIDs = parent.getInts(x - 1, y - 1, width + 2, length + 2);
		final int[] outputBiomeIDs = IntCache.getIntCache(width * length);

		boolean replace = nextInt(4) == 0;
		for (int yIter = 0; yIter < length; ++yIter) {
			for (int xIter = 0; xIter < width; ++xIter) {
				initChunkSeed(xIter + x, yIter + y);
				final int currentBiomeId = inputBiomeIDs[xIter + 1 + (yIter + 1) * (width + 2)];

				if (replace) {
					int newBiomeId = currentBiomeId;

					if (currentBiomeId == BiomeGenBase.desert.biomeID) {
						newBiomeId = BuildCraftEnergy.biomeOilDesert.biomeID;
					}

					outputBiomeIDs[xIter + yIter * width] = newBiomeId;
				} else {
					outputBiomeIDs[xIter + yIter * width] = currentBiomeId;
				}
			}
		}

		return outputBiomeIDs;
	}
}
