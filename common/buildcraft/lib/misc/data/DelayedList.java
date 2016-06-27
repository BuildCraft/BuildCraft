package buildcraft.lib.misc.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

/** Implements a delayed list of something- stuff that can be postponed for later retrieval. A specialised ordered queue
 * really. */
public class DelayedList<E> {
    private final List<List<E>> elements;
    private final Supplier<List<E>> innerListSupplier;

    public DelayedList() {
        this(new ArrayList<>(), ArrayList::new);
    }

    public static <E> DelayedList<E> createConcurrent() {
        return new DelayedList<>(Collections.synchronizedList(new ArrayList<>()), () -> {
            return Collections.synchronizedList(new ArrayList<>());
        });
    }

    private DelayedList(List<List<E>> actualList, Supplier<List<E>> innerList) {
        elements = actualList;
        innerListSupplier = innerList;
    }

    /** @return The maximum delay value that any of the elements has. */
    public int getMaxDelay() {
        return elements.size();
    }

    /** Advances this list by one, essentially decrementing the delays of every element by one and returning all
     * elements that have a delay of 0.
     * 
     * @return The elements that are no longer on a delay. */
    public List<E> advance() {
        if (elements.size() == 0) {
            return ImmutableList.of();
        }
        return elements.remove(0);
    }

    /** Adds an element that will by returned by {@link #advance()} after it has been called delay times. */
    public void add(int delay, E element) {
        if (delay < 0) {
            delay = 0;
        }
        while (elements.size() < delay + 1) {
            elements.add(innerListSupplier.get());
        }
        elements.get(delay).add(element);
    }

    /** @return The inner data structure used to hold the elements. Most useful for saving the elements for later. */
    public List<List<E>> getAllElements() {
        return elements;
    }

    public void clear() {
        elements.clear();
    }
}
