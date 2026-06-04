// STUB(R.Chen): Forge IForgeRegistry — compile shim.
package net.minecraftforge.registries;

public interface IForgeRegistry<V> {
    void register(V value);
    void registerAll(V... values);
}
