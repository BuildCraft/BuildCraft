package buildcraft.lib.prop;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedNonNullProperty<V> implements IUnlistedProperty<V> {
    public final String name;

    public UnlistedNonNullProperty(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(V value) {
        return value != null;
    }

    @Override
    public Class getType() {
        return Object.class;
    }

    @Override
    public String valueToString(V value) {
        return value.toString();
    }
}
