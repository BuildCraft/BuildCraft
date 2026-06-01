// STUB(R.Chen): Forge Loader — compile shim.
package net.minecraftforge.fml.common;

public class Loader {
    private static final Loader INSTANCE = new Loader();

    public static Loader instance() { return INSTANCE; }

    public ModContainer activeModContainer() { return new ModContainer(); }

    public boolean hasReachedState(LoaderState state) { return true; }
}
