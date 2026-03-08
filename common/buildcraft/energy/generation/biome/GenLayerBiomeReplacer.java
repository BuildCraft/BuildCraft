/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation.biome;

import buildcraft.lib.misc.data.SimplexNoise;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate.ParameterPoint;

import java.util.Random;

//public abstract class GenLayerBiomeReplacer extends GenLayer
public abstract class GenLayerBiomeReplacer {
    public static final int OFFSET_RANGE = 500000;
    protected final double xOffset;
    protected final double zOffset;
    protected final double noiseScale;
    protected final double noiseThreshold;
//    protected final Holder<Biome> newBiomeId;

    public GenLayerBiomeReplacer(long worldSeed, long seed, /*GenLayer parent, */double noiseScale, double noiseThreshold, Holder<Biome> newBiomeId) {
//        super(seed);
//        this.parent = parent;
        this.noiseScale = noiseScale;
        this.noiseThreshold = noiseThreshold;
//        this.newBiomeId = newBiomeId;
        Random rand = new Random(worldSeed);
        xOffset = rand.nextInt(OFFSET_RANGE) - (OFFSET_RANGE / 2);
        zOffset = rand.nextInt(OFFSET_RANGE) - (OFFSET_RANGE / 2);
        if (newBiomeId == null) {
            throw new IllegalArgumentException("This biome isn't registered!");
        }
    }

    protected abstract boolean canReplaceBiome(Holder<Biome> biomeId);

    protected abstract Holder<Biome> getHolder();

    protected abstract ParameterPoint getParameterPoint();

    public boolean doReplace(final Holder<Biome> currentBiomeId, final int x, final int y, final int z) {
        return canReplaceBiome(currentBiomeId) &&
                SimplexNoise.noise(
                        (x + xOffset) * noiseScale,
                        (z + zOffset) * noiseScale
                ) > noiseThreshold;
    }
}
