// STUB(R.Chen): 1.12 Chunk → WorldChunk in 1.20.
package net.minecraft.world.chunk;

public class Chunk {
    public enum EnumCreateEntityType { IMMEDIATE, QUEUED, CHECK }
    public int x, z;

    public Chunk(Object world, int x, int z) {
        this.x = x;
        this.z = z;
    }
}
