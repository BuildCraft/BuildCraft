package buildcraft.lib.misc.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/** A {@link TypedMap} that is identical to {@link TypedMapDirect}, except that children of the specified class are also
 * returned.
 * <br>
 * Specifically that {@link #get(Class)} will return either
 * null or a value which returns true when passed in to the argument class's method {@link Class#isInstance(Object)}. */
public class TypedMapHierarchy<V> implements TypedMap<V> {

    private final Map<Class<?>, Node<?>> nodes = new HashMap<>();

    @Override
    public <T extends V> T get(Class<T> clazz) {
        Node<T> n = getNode(clazz);
        if (n == null) {
            return null;
        }
        return n.getFirstValue();
    }

    @Override
    public <T extends V> void put(T value) {
        Class<T> clazz = (Class<T>) value.getClass();
        Node<T> n = getNode(clazz);
        if (n == null) {
            n = putNode(clazz);
        }
        n.value = value;
    }

    @Nullable
    private <T> Node<T> getNode(Class<T> clazz) {
        return (Node<T>) nodes.get(clazz);
    }

    private <T> Node<T> putNode(Class<T> clazz) {
        Node<T> node = new Node<>(clazz);
        nodes.put(clazz, node);
        for (Class<? super T> cls : getAllDirectParents(clazz)) {
            Node<? super T> n2 = getNode(cls);
            if (n2 == null) {
                n2 = putNode(cls);
            }
            n2.children.add(node);
            node.parents.add(n2);
        }
        return node;
    }

    private static <T> List<Class<? super T>> getAllDirectParents(Class<T> clazz) {
        List<Class<? super T>> list = new ArrayList<>();
        Class<? super T> s = clazz.getSuperclass();
        if (s != null) {
            list.add(s);
        }
        for (Class<?> i : clazz.getInterfaces()) {
            list.add((Class<? super T>) i);
        }
        return list;
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public <T extends V> void remove(T value) {
        Class<T> clazz = (Class<T>) value.getClass();
        Node<T> node = getNode(clazz);
        if (node == null || !Objects.equals(value, node.value)) {
            return;
        }
        node.value = null;
        removeNode(node);
    }

    private <T> void removeNode(Node<T> node) {
        if (node.children.isEmpty()) {
            nodes.remove(node.clazz);
            for (Node<? super T> p : node.parents) {
                p.children.remove(node);
                removeNode(p);
            }
        }
    }

    static class Node<T> {
        final Class<T> clazz;
        final List<Node<? super T>> parents = new ArrayList<>();
        final List<Node<? extends T>> children = new ArrayList<>();
        T value;

        Node(Class<T> clazz) {
            this.clazz = clazz;
        }

        T getFirstValue() {
            if (value != null) {
                return value;
            }
            for (Node<? extends T> child : children) {
                T val = child.getFirstValue();
                if (val != null) {
                    return val;
                }
            }
            return null;
        }
    }
}
