/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import java.util.Random;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.data.SimplexNoise;

@SuppressWarnings("Duplicates")
public abstract class GenLayerBiomeReplacer extends GenLayer {
    public static final int OFFSET_RANGE = 500000;
    protected final double xOffset;
    protected final double zOffset;
    protected final double noiseScale;
    protected final double noiseThreshold;
    protected final int newBiomeId;

    /**
     * @param worldSeed
     * @param seed
     * @param parent
     * @param noiseScale     The scale of the noise field, smaller numbers zoom in the noise field.
     * @param noiseThreshold The strength the field must reach to replace the biome. Larger numbers result in smaller
     *                       patches.
     * @param newBiomeId
     */
    public GenLayerBiomeReplacer(long worldSeed,
                                 long seed,
                                 GenLayer parent,
                                 double noiseScale,
                                 double noiseThreshold,
                                 int newBiomeId) {
        super(seed);
        this.parent = parent;
        this.noiseScale = noiseScale;
        this.noiseThreshold = noiseThreshold;
        this.newBiomeId = newBiomeId;
        Random rand = new Random(worldSeed);
        xOffset = rand.nextInt(OFFSET_RANGE) - (OFFSET_RANGE / 2);
        zOffset = rand.nextInt(OFFSET_RANGE) - (OFFSET_RANGE / 2);
        if (newBiomeId < 0) {
            throw new IllegalArgumentException("This biome isn't registered!");
        }
    }

    protected abstract boolean canReplaceBiome(int biomeId);

    @Override
    public int[] getInts(final int x, final int z, final int width, final int length) {
        final int[] inputBiomeIDs = parent.getInts(x - 1, z - 1, width + 2, length + 2);
        final int[] outputBiomeIDs = IntCache.getIntCache(width * length);
        for (int xIter = 0; xIter < width; ++xIter) {
            for (int zIter = 0; zIter < length; ++zIter) {
                initChunkSeed(xIter + x, zIter + z);
                int currentBiomeId = inputBiomeIDs[xIter + 1 + (zIter + 1) * (width + 2)];
                if (canReplaceBiome(currentBiomeId) &&
                        SimplexNoise.noise(
                                (xIter + x + xOffset) * noiseScale,
                                (zIter + z + zOffset) * noiseScale
                        ) > noiseThreshold) {
                    outputBiomeIDs[xIter + zIter * width] = newBiomeId;
                } else {
                    outputBiomeIDs[xIter + zIter * width] = currentBiomeId;
                }
            }
        }
        return outputBiomeIDs;
    }
}
