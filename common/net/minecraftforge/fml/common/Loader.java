// STUB(R.Chen): Forge Loader — compile shim.
package net.minecraftforge.fml.common;

public class Loader {
    private static final Loader INSTANCE = new Loader();

    public static Loader instance() { return INSTANCE; }

    public ModContainer activeModContainer() { return new ModContainer(); }

    public boolean hasReachedState(LoaderState state) { return true; }
    public static boolean isModLoaded(String modId) {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
    }
    public String getDisplayVersion() { return "1.0.0"; }
    public String getMod() { return "buildcraft"; }
    public java.io.File getGameDirectory() { return new java.io.File("."); }
}
