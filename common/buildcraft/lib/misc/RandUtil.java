/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Random;

// Yarn 1.20.1: net.minecraft.world.World is still the correct package.
// TODO(R.Chen): world.getSeed() moved to ServerWorld in 1.20.1. Either change
// the createRandomForChunk(World,...) signature to take ServerWorld, or cast
// at the call site. Behavioral seed math below is unchanged.
import net.minecraft.world.World;

/** Utilities based around more complex (but common) usages of {@link Random}. */
public class RandUtil {
    /** Creates a {@link Random} instance for a specific generator, for the specified chunk, in the specified world.
     * 
     * @param world The world to generate for.
     * @param chunkX The chunk X co-ord to generate for.
     * @param chunkY The chunk X co-ord to generate for.
     * @param magicNumber The magic number, specific to the generator. Each different generator that calls this should
     *            have a different number, so that different generators don't start by generating structures in the same
     *            place. It is recommended that you generate a random number once, and place it statically in the
     *            generator class (Perhaps by using <code>new SecureRandom().nextLong()</code>).
     * @return A {@link Random} instance that starts off with the same seed given the same arguments. */
    public static Random createRandomForChunk(World world, int chunkX, int chunkY, long magicNumber) {
        long worldSeed = world.getSeed();
        return createRandomForChunk(worldSeed, chunkX, chunkY, magicNumber);
    }

    /** Creates a {@link Random} instance for a specific generator, for the specified chunk, for a given world seed
     * 
     * @param worldSeed The seed of a world to generate for.
     * @param chunkX The chunk X co-ord to generate for.
     * @param chunkY The chunk X co-ord to generate for.
     * @param magicNumber The magic number, specific to the generator. Each different generator that calls this should
     *            have a different number, so that different generators don't start by generating structures in the same
     *            place. It is recommended that you generate a random number once, and place it statically in the
     *            generator class (Perhaps by using <code>new SecureRandom().nextLong()</code>).
     * @return A {@link Random} instance that starts off with the same seed given the same arguments. */
    public static Random createRandomForChunk(long worldSeed, int chunkX, int chunkY, long magicNumber) {
        // Ensure we have the same seed for the same chunk
        // (this is similar to the code that calls IWorldGenerator.generate)
        Random worldRandom = new Random(worldSeed);
        long xSeed = worldRandom.nextLong() >> 2 + 1L;
        long zSeed = worldRandom.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * chunkX + zSeed * chunkY) ^ worldSeed;
        // XOR our own number so that we differ from other generators
        chunkSeed ^= magicNumber;
        return new Random(chunkSeed);
    }
}
