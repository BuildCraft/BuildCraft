package buildcraft.core.lib.event;

public interface IEventHandler<T> {
    void handle(T event);
}
