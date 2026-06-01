// STUB(R.Chen): Forge FMLCommonHandler — compile shim.
package net.minecraftforge.fml.common;

public class FMLCommonHandler {
    private static final FMLCommonHandler INSTANCE = new FMLCommonHandler();
    public static FMLCommonHandler instance() { return INSTANCE; }
    public net.minecraft.server.MinecraftServer getMinecraftServerInstance() { return null; }
    public boolean isClient() { return false; }
    public boolean isServer() { return true; }
}
