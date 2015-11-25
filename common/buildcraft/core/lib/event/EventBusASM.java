package buildcraft.core.lib.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class EventBusASM<T> implements IEventBus<T> {
    private final Multimap<Object, IEventHandler<T>> handlers = HashMultimap.create();
    private final EventBusProviderASM<T> provider;
    private final Class<T> eventClass;

    public EventBusASM(EventBusProviderASM<T> provider, Class<T> eventClass) {
        this.provider = provider;
        this.eventClass = eventClass;
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
        for (IEventHandler<T> handler : handlers.values()) {
            handler.handle(event);
        }
    }
}
