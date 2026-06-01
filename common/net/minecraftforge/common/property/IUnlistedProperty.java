// STUB(R.Chen): Forge IUnlistedProperty — removed in 1.20. No direct equivalent; unlisted state was Forge-only.
package net.minecraftforge.common.property;

public interface IUnlistedProperty<V> {
    String getName();
    boolean isValid(V value);
    Class<V> getType();
    String valueToString(V value);
}
