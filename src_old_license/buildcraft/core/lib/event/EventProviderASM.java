package buildcraft.core.lib.event;

import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class EventProviderASM<T> {
    private final List<IEventHandlerProvider<T>> handlerProviders;

    public EventProviderASM(List<IEventHandlerProvider<T>> handlerProviders) {
        this.handlerProviders = ImmutableList.copyOf(handlerProviders);
    }

    public List<IEventHandler<T>> getNewHandlerSet(Object obj) {
        List<IEventHandler<T>> handlers = Lists.newArrayList();
        for (IEventHandlerProvider<T> provider : handlerProviders) {
            handlers.add(provider.createNewHandler(obj));
        }
        return handlers;
    }
}
