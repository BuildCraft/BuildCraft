// STUB(R.Chen): Forge PopulateChunkEvent — compile shim.
package net.minecraftforge.event.terraingen;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PopulateChunkEvent extends Event {
    private final World world;
    private final int chunkX;
    private final int chunkZ;

    public PopulateChunkEvent(World world, int chunkX, int chunkZ) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public World getWorld() { return world; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }

    public enum EventType { ANIMALS, DUNGEON, FIRE, GLOWSTONE, ICE, LAKE, LAVA, NETHER_LAVA, NETHER_LAVA2, CUSTOM }

    public static class Populate extends PopulateChunkEvent {
        public Populate(World world, int chunkX, int chunkZ) { super(world, chunkX, chunkZ); }

        public enum EventType { ANIMALS, DUNGEON, FIRE, GLOWSTONE, ICE, LAKE, LAVA, NETHER_LAVA, NETHER_LAVA2, CUSTOM }
    }

    public static class Post extends PopulateChunkEvent {
        private final Random rand;
        private final Object gen;
        private final boolean hasVillageGenerated;

        public Post(World world, Object gen, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated) {
            super(world, chunkX, chunkZ);
            this.gen = gen;
            this.rand = rand;
            this.hasVillageGenerated = hasVillageGenerated;
        }

        public Random getRand() { return rand; }
        public Object getGen() { return gen; }
        public boolean isHasVillageGenerated() { return hasVillageGenerated; }
    }

    public static class Pre extends PopulateChunkEvent {
        public Pre(World world, int chunkX, int chunkZ) { super(world, chunkX, chunkZ); }
    }
}
