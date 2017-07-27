package buildcraft.lib.expression.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToObject.IFuncObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong.IFuncObjectToLong;

public final class NodeType2<T> {
    public final Class<T> type;
    public final T defaultValue;
    public final FunctionContext objectContext;
    private final Map<Class<?>, Function<?, T>> casts = new HashMap<>();

    public NodeType2(T defaultValue) {
        this.type = (Class<T>) defaultValue.getClass();
        this.defaultValue = defaultValue;
        objectContext = new FunctionContext();
    }

    public NodeType2(Class<T> type, T defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
        objectContext = new FunctionContext();
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

    public <F> void putCast(Class<F> from, Function<F, T> function) {
        casts.put(from, function);
    }

    public <F> Function<F, T> getCast(Class<F> from) {
        return (Function<F, T>) casts.get(from);
    }

    public void put_t_l(String name, IFuncObjectToLong<T> func) {
        objectContext.put_o_l(name, type, func);
    }

    public void put_t_t(String name, Function<T, T> func) {
        objectContext.put_o_o(name, type, type, func);
    }

    public <O> void put_t_o(String name, Class<O> clazz, Function<T, O> func) {
        objectContext.put_o_o(name, type, clazz, func);
    }

    public <O> void put_tl_t(String name, IFuncObjectLongToObject<T, T> func) {
        objectContext.put_ol_o(name, type, type, func);
    }
}
