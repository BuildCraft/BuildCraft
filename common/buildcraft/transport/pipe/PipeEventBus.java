package buildcraft.transport.pipe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

import buildcraft.api.transport.PipeEvent;
import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventPriority;

public class PipeEventBus {
    private static final Map<Class<?>, List<Handler>> allHandlers = new HashMap<>();

    private final List<LocalHandler> currentHandlers = new ArrayList<>();

    private static List<LocalHandler> getAndBindHandlers(Object obj) {
        Class<?> cls = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        List<Handler> handlerList = getHandlers(cls);
        List<LocalHandler> list = new ArrayList<>();
        for (Handler handler : handlerList) {
            LocalHandler bound = handler.bindTo(obj);
            /* The handler will be null if a class was registered but the method was not static */
            if (bound != null) {
                list.add(bound);
            }
        }
        return list;
    }

    private static List<Handler> getHandlers(Class<?> cls) {
        if (!allHandlers.containsKey(cls)) {
            List<Handler> list = new ArrayList<>();
            for (Method m : cls.getMethods()) {
                PipeEventHandler annot = m.getAnnotation(PipeEventHandler.class);
                if (annot == null) {
                    continue;
                }

                Parameter[] params = m.getParameters();
                if (params.length != 1) {
                    throw new IllegalStateException("Cannot annotate " + m + " with @PipeEventHandler as it had an incorrect number of paramaters (" + Arrays.toString(params) + ")");
                }
                Parameter p = params[0];
                if (!PipeEvent.class.isAssignableFrom(p.getType())) {
                    throw new IllegalStateException("Cannot annotate " + m + " with @PipeEventHandler as it did not take a pipe event! (" + p.getType() + ")");
                }

                MethodHandle mh;
                try {
                    mh = MethodHandles.publicLookup().unreflect(m);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot annotate " + m + " with @PipeEventHandler as there was a problem with it!", e);
                }
                boolean isStatic = Modifier.isStatic(m.getModifiers());
                list.add(new Handler(annot.priority(), annot.receiveCancelled(), isStatic, mh, p.getType()));
            }

            Class<?> superCls = cls.getSuperclass();
            if (superCls != null) {
                list.addAll(getHandlers(superCls));
            }
            allHandlers.put(cls, list);
            return list;
        }
        return allHandlers.get(cls);
    }

    public void registerHandler(Object obj) {
        if (obj == null) {
            return;
        }
        currentHandlers.addAll(getAndBindHandlers(obj));
        Collections.sort(currentHandlers);
    }

    public void unregisterHandler(Object obj) {
        if (obj == null) {
            return;
        }

        Iterator<LocalHandler> iter = currentHandlers.iterator();
        while (iter.hasNext()) {
            LocalHandler next = iter.next();
            if (next.target == obj) {
                iter.remove();
            }
        }
    }

    public boolean fireEvent(PipeEvent event) {
        boolean handled = false;
        for (LocalHandler handler : currentHandlers) {
            handled |= handler.handleEvent(event);
        }
        return handled;
    }

    public static class Handler {
        final PipeEventPriority priority;
        final boolean receiveCanceled, isStatic;
        final MethodHandle handle;
        final Class<?> eventClassHandled;

        public Handler(PipeEventPriority priority, boolean receiveCanceled, boolean isStatic, MethodHandle handle, Class<?> eventClassHandled) {
            this.priority = priority;
            this.receiveCanceled = receiveCanceled;
            this.isStatic = isStatic;
            this.handle = handle;
            this.eventClassHandled = eventClassHandled;
        }

        public LocalHandler bindTo(Object obj) {
            // If its not a static method then we cannot pass the class to the handler, so we won't bind it
            if (!isStatic && obj instanceof Class<?>) {
                return null;
            }
            MethodHandle bound = isStatic ? handle : handle.bindTo(obj);
            return new LocalHandler(priority, receiveCanceled, obj, eventClassHandled, bound);
        }
    }

    public static class LocalHandler implements Comparable<LocalHandler> {
        final PipeEventPriority priority;
        final boolean receiveCanceled;
        final Object target;
        final Class<?> classHandled;
        final MethodHandle handle;

        public LocalHandler(PipeEventPriority priority, boolean receiveCanceled, Object target, Class<?> classHandled, MethodHandle handle) {
            this.priority = priority;
            this.receiveCanceled = receiveCanceled;
            this.target = target;
            this.classHandled = classHandled;
            this.handle = handle;
        }

        public boolean handleEvent(PipeEvent event) {
            if (!receiveCanceled && event.isCanceled()) {
                return false;
            }

            if (classHandled.isAssignableFrom(event.getClass())) {
                try {
                    handle.invoke(event);
                    return true;
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
            return false;
        }

        @Override
        public int compareTo(LocalHandler o) {
            return priority.compareTo(o.priority);
        }
    }
}
