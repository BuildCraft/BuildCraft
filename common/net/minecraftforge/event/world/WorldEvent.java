// STUB(R.Chen): Forge WorldEvent — compile shim.
package net.minecraftforge.event.world;

import net.minecraft.world.World;

public class WorldEvent {
    private final World world;
    public WorldEvent(World world) { this.world = world; }
    public World getWorld() { return world; }

    public static class Load extends WorldEvent {
        public Load(World world) { super(world); }
    }

    public static class Unload extends WorldEvent {
        public Unload(World world) { super(world); }
    }

    public static class Save extends WorldEvent {
        public Save(World world) { super(world); }
    }
}
