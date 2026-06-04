// STUB(R.Chen): Forge ForgeChunkManager — compile shim. Replaced by buildcraft.lib.chunkload.ChunkLoaderManager.
package net.minecraftforge.common;

import net.minecraft.world.World;

import buildcraft.lib.chunkload.ChunkLoaderManager;

public class ForgeChunkManager {

    public static FakeConfig getConfig() { return new FakeConfig(); }
    public static void syncConfigDefaults() {}

    public interface LoadingCallback {
        void ticketsLoaded(Object tickets, World world);
    }

    public static void setForcedChunkLoadingCallback(Object mod, ChunkLoaderManager.RebindCallback callback) {
        // no-op — handled by ChunkLoaderManager Fabric implementation
    }

    public static class FakeConfig {
        public FakeProperty get(String modId, String key, int defaultValue) { return new FakeProperty(); }
    }

    public static class FakeProperty {
        public int getInt() { return 26; }
    }
}
