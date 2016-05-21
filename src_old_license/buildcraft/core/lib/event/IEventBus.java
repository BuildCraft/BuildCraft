package buildcraft.core.lib.event;

public interface IEventBus<T> {

    void registerHandler(Object handler);

    void unregisterHandler(Object handler);

    void handleEvent(T event);
}
