package buildcraft.transport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import buildcraft.transport.pipes.events.PipeEvent;
import buildcraft.transport.pipes.events.PipeEventPriority;

public class PipeEventBus {
	private class EventHandler {
		public Method method;
		public Object owner;

		public EventHandler(Method m, Object o) {
			this.method = m;
			this.owner = o;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof EventHandler)) {
				return false;
			}

			EventHandler e = (EventHandler) o;
			return e.method.equals(method) && e.owner == owner;
		}
	}

	private static class EventHandlerCompare implements Comparator<EventHandler> {
		private int getPriority(EventHandler eh) {
			PipeEventPriority p = eh.method.getAnnotation(PipeEventPriority.class);
			return p != null ? p.priority() : 0;
		}

		@Override
		public int compare(EventHandler o1, EventHandler o2) {
			int priority1 = getPriority(o1);
			int priority2 = getPriority(o2);
			return priority2 - priority1;
		}
	}

	private static final EventHandlerCompare COMPARATOR = new EventHandlerCompare();
	private static final HashSet<Object> globalHandlers = new HashSet<Object>();

	private final HashSet<Object> registeredHandlers = new HashSet<Object>();
	private final HashMap<Object, Map<Method, Class<? extends PipeEvent>>> handlerMethods = new HashMap<Object, Map<Method, Class<? extends PipeEvent>>>();
	private final HashMap<Class<? extends PipeEvent>, List<EventHandler>> eventHandlers = new HashMap<Class<? extends PipeEvent>, List<EventHandler>>();

	public PipeEventBus() {
		for (Object o : globalHandlers) {
			registerHandler(o);
		}
	}

	public static void registerGlobalHandler(Object globalHandler) {
		globalHandlers.add(globalHandler);
	}

	private List<EventHandler> getHandlerList(Class<? extends PipeEvent> event) {
		if (!eventHandlers.containsKey(event)) {
			eventHandlers.put(event, new ArrayList<EventHandler>());
		}
		return eventHandlers.get(event);
	}

	public void registerHandler(Object handler) {
		if (registeredHandlers.contains(handler)) {
			return;
		}

		registeredHandlers.add(handler);
		Map<Method, Class<? extends PipeEvent>> methods = new HashMap<Method, Class<? extends PipeEvent>>();

		for (Method m : handler.getClass().getDeclaredMethods()) {
			if ("eventHandler".equals(m.getName())) {
				Class<?>[] parameters = m.getParameterTypes();
				if (parameters.length == 1 && PipeEvent.class.isAssignableFrom(parameters[0])) {
					Class<? extends PipeEvent> eventType = (Class<? extends PipeEvent>) parameters[0];
					List<EventHandler> eventHandlerList = getHandlerList(eventType);
					eventHandlerList.add(new EventHandler(m, handler));
					updateEventHandlers(eventHandlerList);
					methods.put(m, eventType);
				}
			}
		}

		handlerMethods.put(handler, methods);
	}

	private void updateEventHandlers(List<EventHandler> eventHandlerList) {
		Collections.sort(eventHandlerList, COMPARATOR);
	}

	public void unregisterHandler(Object handler) {
		if (!registeredHandlers.contains(handler)) {
			return;
		}

		registeredHandlers.remove(handler);
		Map<Method, Class<? extends PipeEvent>> methodMap = handlerMethods.get(handler);
		for (Method m : methodMap.keySet()) {
			getHandlerList(methodMap.get(m)).remove(new EventHandler(m, handler));
		}
		handlerMethods.remove(handler);
	}

	public void handleEvent(Class<? extends PipeEvent> eventClass, PipeEvent event) {
		for (EventHandler eventHandler : getHandlerList(eventClass)) {
			try {
				eventHandler.method.invoke(eventHandler.owner, event);
			} catch (Exception e) {

			}
		}
	}
}
