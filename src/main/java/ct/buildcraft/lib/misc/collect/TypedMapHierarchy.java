package ct.buildcraft.lib.misc.collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/** A {@link TypedMap} that is identical to {@link TypedMapDirect}, except that children of the specified class are also
 * returned.<br>
 * Specifically that {@link #get(Class)} will return either null or a value which returns true when passed in to the
 * argument class's method {@link Class#isInstance(Object)}. */
public class TypedMapHierarchy<V> implements TypedMap<V> {

    private final Map<Class<?>, Node<?>> nodes = new HashMap<>();

    @Override
    public <T extends V> T get(Class<T> clazz) {
        Node<?> node = nodes.get(clazz);
        if (node == null) {
            return null;
        }
        return clazz.cast(node.getFirstValue());
    }

    @Override
    public void put(V value) {
        Class<?> clazz = value.getClass();
        Node<?> node = nodes.get(clazz);
        if (node == null) {
            node = putNode(clazz);
        }
        node.setValue(value);
    }

    @Nullable
    private Node<?> getNode(Class<?> clazz) {
        return nodes.get(clazz);
    }

    private <T> Node<T> putNode(Class<T> clazz) {
        Node<T> node = new Node<>(clazz);
        nodes.put(clazz, node);
        for (Class<?> cls : getAllDirectParents(clazz)) {
            Node<?> oNode = nodes.get(cls);
            if (oNode == null) {
                oNode = putNode(cls);
            }
            oNode.children.add(node);
            node.parents.add(oNode);
        }
        return node;
    }

    private static <T> List<Class<?>> getAllDirectParents(Class<T> clazz) {
        List<Class<?>> list = new ArrayList<>();
        Class<? super T> s = clazz.getSuperclass();
        if (s != null) {
            list.add(s);
        }
        Collections.addAll(list, clazz.getInterfaces());
        return list;
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public void remove(V value) {
        Class<?> clazz = value.getClass();
        Node<?> node = getNode(clazz);
        if (node == null || !Objects.equals(value, node.value)) {
            return;
        }
        node.value = null;
        removeNode(node);
    }

    private <T> void removeNode(Node<T> node) {
        if (node.children.isEmpty()) {
            nodes.remove(node.clazz);
            for (Node<?> p : node.parents) {
                p.children.remove(node);
                removeNode(p);
            }
        }
    }

    static class Node<T> {
        final Class<T> clazz;
        final List<Node<?>> parents = new ArrayList<>();
        final List<Node<?>> children = new ArrayList<>();
        T value;

        Node(Class<T> clazz) {
            this.clazz = clazz;
        }

        void setValue(Object newValue) {
            value = clazz.cast(newValue);
        }

        T getFirstValue() {
            if (value != null) {
                return value;
            }
            for (Node<?> child : children) {
                Object val = child.getFirstValue();
                if (val != null) {
                    return clazz.cast(val);
                }
            }
            return null;
        }
    }
}
