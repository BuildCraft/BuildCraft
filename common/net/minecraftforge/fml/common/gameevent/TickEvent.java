// STUB(R.Chen): Forge TickEvent — compile shim.
package net.minecraftforge.fml.common.gameevent;

import net.minecraft.world.World;

public class TickEvent {
    public enum Phase { START, END }

    public Phase phase;

    public static class ClientTickEvent extends TickEvent {}
    public static class ServerTickEvent extends TickEvent {}
    public static class RenderTickEvent extends TickEvent {
        public float renderTickTime;
    }
    public static class WorldTickEvent extends TickEvent {
        public World world;
        public WorldTickEvent(World world, Phase phase) {
            this.world = world;
            this.phase = phase;
        }
        public WorldTickEvent() {}
        public World getWorld() { return world; }
    }
}
