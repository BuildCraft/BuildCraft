// STUB(R.Chen): Minecraft 1.12 GenLayer — replaced by biome source system in 1.20.1.
package net.minecraft.world.gen.layer;

public abstract class GenLayer {
    protected long worldGenSeed;
    protected long chunkSeed;
    protected GenLayer parent;

    public GenLayer(long seed) { this.worldGenSeed = seed; }

    public static GenLayer[] initializeAllBiomeGenerators(long seed, net.minecraft.world.WorldType worldType) {
        return new GenLayer[0];
    }

    public void initWorldGenSeed(long seed) { this.worldGenSeed = seed; }

    public abstract int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight);
}
