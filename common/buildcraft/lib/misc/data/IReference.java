package buildcraft.lib.misc.data;

public interface IReference<T> {
    T get();

    void set(T to);
}
