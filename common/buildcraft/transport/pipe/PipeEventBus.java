/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPriority;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

public class PipeEventBus {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("transport.pipe.event_bus");
    // TODO: Investigate using a flag long to check if any handlers are registered for a PipeEvent class!

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
            Class<?> superCls = cls.getSuperclass();
            if (superCls != null) {
                list.addAll(getHandlers(superCls));
            }
            for (Method m : cls.getDeclaredMethods()) {
                PipeEventHandler annot = m.getAnnotation(PipeEventHandler.class);
                if (annot == null) {
                    continue;
                }

                Parameter[] params = m.getParameters();
                if (params.length != 1) {
                    throw new IllegalStateException("Cannot annotate " + m + " with @PipeEventHandler as it had an incorrect number of parameters (" + Arrays.toString(params) + ")");
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
                String methodName = m.toString();
                list.add(new Handler(annot.priority(), annot.receiveCancelled(), isStatic, methodName, mh, p.getType()));
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

        currentHandlers.removeIf(next -> next.target == obj);
    }

    /** Sends this event to all of the registered handlers.
     *
     * @return True if at least 1 event handler was called, 0 if no handlers were called. */
    public boolean fireEvent(PipeEvent event) {
        boolean handled = false;
        if (DEBUG) {
            String error = event.checkStateForErrors();
            if (error != null) {
                throw new IllegalArgumentException("The event " + event.getClass() + " was in an invalid state when firing! This is DEFINITELY a bug!\n"//
                        + "(error = " + error + ")");
            }
        }
        for (LocalHandler handler : currentHandlers) {
            handled |= handler.handleEvent(event);
            if (DEBUG) {
                String error = event.checkStateForErrors();
                if (error != null) {
                    throw new IllegalStateException("The event " + event.getClass() + " was in an invalid state after being handled by "//
                            + handler.methodName + " (error = " + error + ")");
                }
            }
        }
        return handled;
    }

    public static class Handler {
        final PipeEventPriority priority;
        final boolean receiveCanceled, isStatic;
        final String methodName;
        final MethodHandle handle;
        final Class<?> eventClassHandled;

        public Handler(PipeEventPriority priority, boolean receiveCanceled, boolean isStatic, String methodName, MethodHandle handle, Class<?> eventClassHandled) {
            this.priority = priority;
            this.receiveCanceled = receiveCanceled;
            this.isStatic = isStatic;
            this.methodName = methodName;
            this.handle = handle;
            this.eventClassHandled = eventClassHandled;
        }

        public LocalHandler bindTo(Object obj) {
            // If its not a static method then we cannot pass the class to the handler, so we won't bind it
            if (!isStatic && obj instanceof Class<?>) {
                return null;
            }
            MethodHandle bound = isStatic ? handle : handle.bindTo(obj);
            return new LocalHandler(priority, receiveCanceled, obj, methodName, eventClassHandled, bound);
        }
    }

    public static class LocalHandler implements Comparable<LocalHandler> {
        final PipeEventPriority priority;
        final boolean receiveCanceled;
        final Object target;
        final String methodName;
        final Class<?> classHandled;
        final MethodHandle handle;

        public LocalHandler(PipeEventPriority priority, boolean receiveCanceled, Object target, String methodName, Class<?> classHandled, MethodHandle handle) {
            this.priority = priority;
            this.receiveCanceled = receiveCanceled;
            this.target = target;
            this.methodName = methodName;
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
