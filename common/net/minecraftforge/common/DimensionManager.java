// STUB(R.Chen): Forge DimensionManager — compile shim. TODO: replace with vanilla ServerWorld lookups.
package net.minecraftforge.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class DimensionManager {
    public static ServerWorld getWorld(int dim) { return null; }
    public static int[] getIDs() { return new int[0]; }
    public static int[] getStaticDimensionIDs() { return new int[0]; }
    public static boolean isDimensionRegistered(int dim) { return false; }
}
