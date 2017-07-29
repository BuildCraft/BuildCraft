package buildcraft.lib.expression.api;

import java.util.HashMap;
import java.util.Map;

import buildcraft.lib.expression.NodeTypeBase;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject.IFuncObjectToObject;

public final class NodeType2<T> extends NodeTypeBase<T> {
    public final Class<T> type;
    public final T defaultValue;

    public NodeType2(T defaultValue) {
        this((Class<T>) defaultValue.getClass(), defaultValue);
    }

    public NodeType2(Class<T> type, T defaultValue) {
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
        return type == ((NodeType2<?>) obj).type;
    }

    public void putConstant(String name, T value) {
        putConstant(name, type, value);
    }
}
