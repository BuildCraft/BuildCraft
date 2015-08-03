package buildcraft.core.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ListRegistry {
	private static final List<ListMatchHandler> handlers = new ArrayList<ListMatchHandler>();

	private ListRegistry() {

	}

	public static void registerHandler(ListMatchHandler h) {
		if (h != null) {
			handlers.add(h);
		}
	}

	public static List getHandlers() {
		return Collections.unmodifiableList(handlers);
	}
}
