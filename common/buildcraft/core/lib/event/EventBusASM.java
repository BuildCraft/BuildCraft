package buildcraft.core.lib.event;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

public class EventBusASM<T> implements IEventBus<T> {
    private final Multimap<Object, IEventHandler<T>> handlers = HashMultimap.create();
    private final EventBusProviderASM<T> provider;

    public EventBusASM(EventBusProviderASM<T> provider) {
        this.provider = provider;
    }

    @Override
    public void registerHandler(Object handler) {
        EventProviderASM<T> eventProvider = provider.getProviderFor(handler.getClass());
        handlers.putAll(handler, eventProvider.getNewHandlerSet(handler));
    }

    @Override
    public void unregisterHandler(Object handler) {
        handlers.removeAll(handler);
    }

    @Override
    public void handleEvent(T event) {
        // This allows handlers to be registered and unregistered at any time, event during event firing.
        Collection<IEventHandler<T>> handlers = this.handlers.values();
        handlers = ImmutableList.copyOf(handlers);
        for (IEventHandler<T> handler : handlers) {
            handler.handle(event);
        }
    }
}
