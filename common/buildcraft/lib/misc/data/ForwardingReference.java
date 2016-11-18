package buildcraft.lib.misc.data;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForwardingReference<T> implements IReference<T> {

    public final Supplier<T> getter;
    public final Consumer<T> setter;

    public ForwardingReference(Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public void set(T to) {
        setter.accept(to);
    }
}
