package buildcraft.lib.misc.data;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForwardingReference<T> implements IReference<T> {

    public final Supplier<T> getter;
    public final Consumer<T> setter;
    public final Class<T> clazz;

    public ForwardingReference(Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
        this.clazz = clazz;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public void set(T to) {
        setter.accept(to);
    }

    @Override
    public boolean canSet(Object value) {
        return clazz.isInstance(value);
    }
}
