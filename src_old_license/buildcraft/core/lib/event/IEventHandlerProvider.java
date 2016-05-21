package buildcraft.core.lib.event;

public interface IEventHandlerProvider<T> {
    public IEventHandler<T> createNewHandler(Object obj);
}
