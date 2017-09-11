package buildcraft.lib.expression.api;

import buildcraft.lib.expression.NodeTypeBase;

public final class NodeType<T> extends NodeTypeBase<T> {
    public final Class<T> type;
    public final T defaultValue;

    public NodeType(T defaultValue) {
        this((Class<T>) defaultValue.getClass(), defaultValue);
    }

    public NodeType(Class<T> type, T defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    protected Class<T> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        return type == ((NodeType<?>) obj).type;
    }

    public void putConstant(String name, T value) {
        putConstant(name, type, value);
    }
}
