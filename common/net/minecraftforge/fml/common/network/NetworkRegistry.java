// STUB(R.Chen): Forge NetworkRegistry — compile shim.
package net.minecraftforge.fml.common.network;

public class NetworkRegistry {
    public static final NetworkRegistry INSTANCE = new NetworkRegistry();

    public void registerGuiHandler(Object mod, IGuiHandler handler) {
        // no-op — GUI handling replaced by Fabric screen factories
    }
}
