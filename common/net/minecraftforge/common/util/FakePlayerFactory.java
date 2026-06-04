// STUB(R.Chen): Forge FakePlayerFactory — compile shim.
package net.minecraftforge.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.world.ServerWorld;

public final class FakePlayerFactory {
    private FakePlayerFactory() {}

    public static FakePlayer get(ServerWorld world, GameProfile profile) {
        return new FakePlayer(world, profile);
    }

    /** Nested target type used by MessageManager.sendToAllAround (compile shim only). */
    public static class Target {
        public final double x, y, z;
        public final double radius;
        public final int dimension;
        public Target(int dimension, double x, double y, double z, double radius) {
            this.dimension = dimension; this.x = x; this.y = y; this.z = z; this.radius = radius;
        }
    }
}
