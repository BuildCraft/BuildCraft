package buildcraft.lib.misc.data;

/** Defines a simple reference to an object, that can be retried or changed at any time. */
public interface IReference<T> {
    T get();

    void set(T to);
}
