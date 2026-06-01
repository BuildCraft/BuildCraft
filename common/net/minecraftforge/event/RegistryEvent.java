// STUB(R.Chen): Forge RegistryEvent — compile shim.
package net.minecraftforge.event;

public class RegistryEvent<T> {
    public static class Register<T> extends RegistryEvent<T> {
        public net.minecraftforge.registries.IForgeRegistry<T> getRegistry() { return null; }
    }
}
