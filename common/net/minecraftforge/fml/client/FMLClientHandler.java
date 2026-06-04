// STUB(R.Chen): Forge FMLClientHandler — compile shim.
package net.minecraftforge.fml.client;

import net.minecraft.client.MinecraftClient;

public class FMLClientHandler {
    private static final FMLClientHandler INSTANCE = new FMLClientHandler();
    public static FMLClientHandler instance() { return INSTANCE; }
    public MinecraftClient getClient() { return MinecraftClient.getInstance(); }
    public boolean isJoinedToServer() { return MinecraftClient.getInstance().world != null; }
}
