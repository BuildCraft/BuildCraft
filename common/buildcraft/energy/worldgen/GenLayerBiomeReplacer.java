/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.worldgen;

import java.util.Random;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import buildcraft.core.lib.utils.SimplexNoise;

public abstract class GenLayerBiomeReplacer extends GenLayer {

	public static final int OFFSET_RANGE = 500000;
	protected final double xOffset;
	protected final double zOffset;
	protected final double noiseScale;
	protected final double noiseThreshold;
	protected final int newBiomeId;

	/**
	 *
	 * @param worldSeed
	 * @param seed
	 * @param parent
	 * @param noiseScale The scale of the noise field, smaller numbers zoom in
	 * the noise field.
	 * @param noiseThreshold The strength the field must reach to replace the
	 * biome. Larger numbers result in smaller patches.
	 * @param newBiomeId
	 */
	public GenLayerBiomeReplacer(final long worldSeed, final long seed, final GenLayer parent, double noiseScale, double noiseThreshold, int newBiomeId) {
		super(seed);
		this.parent = parent;
		this.noiseScale = noiseScale;
		this.noiseThreshold = noiseThreshold;
		this.newBiomeId = newBiomeId;
		Random rand = new Random(worldSeed);
		xOffset = rand.nextInt(OFFSET_RANGE) - (OFFSET_RANGE / 2);
		zOffset = rand.nextInt(OFFSET_RANGE) - (OFFSET_RANGE / 2);
	}

	protected abstract boolean canReplaceBiome(int biomeId);

	@Override
	public int[] getInts(final int x, final int z, final int width, final int length) {
		final int[] inputBiomeIDs = parent.getInts(x - 1, z - 1, width + 2, length + 2);
		final int[] outputBiomeIDs = IntCache.getIntCache(width * length);
		for (int xIter = 0; xIter < width; ++xIter) {
			for (int zIter = 0; zIter < length; ++zIter) {
				initChunkSeed(xIter + x, zIter + z);
				final int currentBiomeId = inputBiomeIDs[xIter + 1 + (zIter + 1) * (width + 2)];
				if (canReplaceBiome(currentBiomeId) && SimplexNoise.noise((xIter + x + xOffset) * noiseScale, (zIter + z + zOffset) * noiseScale) > noiseThreshold) {
					outputBiomeIDs[xIter + zIter * width] = newBiomeId;
//					System.out.printf("Replaced Biome at %d, %d\n", xIter + x, zIter + z);
				} else {
					outputBiomeIDs[xIter + zIter * width] = currentBiomeId;
				}
			}
		}
		return outputBiomeIDs;
	}
}
