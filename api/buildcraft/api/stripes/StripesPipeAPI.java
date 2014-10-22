package buildcraft.api.stripes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public final class StripesPipeAPI {
	private static final LinkedList<IStripesItemHandler> handlers = new LinkedList<IStripesItemHandler>();
	
	private StripesPipeAPI() {
	}
	
	public static Collection<IStripesItemHandler> getHandlerList() {
		return Collections.unmodifiableCollection(handlers);
	}
	
	public static void registerHandler(IStripesItemHandler handler) {
		if (!handlers.contains(handler)) {
			handlers.add(handler);
		}
	}
}
