package buildcraft.core.lib.event;

public interface IEventBusProvider<T> {
    IEventBus<T> newBus();
}
