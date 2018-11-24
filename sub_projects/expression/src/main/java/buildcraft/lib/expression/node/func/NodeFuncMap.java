package buildcraft.lib.expression.node.func;

import java.util.EnumMap;
import java.util.Map;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeObject;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public class NodeFuncMap<K extends Enum<K>, V> implements INodeFuncObject<V> {
    private final Class<K> keyClass;
    private final Class<V> valueClass;
    private final Map<K, NodeVariableObject<V>> variableMap;
    private final NodeVariableObject<V> nullEntry;

    public NodeFuncMap(Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.variableMap = new EnumMap<>(keyClass);
        for (K enumKey : keyClass.getEnumConstants()) {
            variableMap.put(enumKey, new NodeVariableObject<>("entry_" + enumKey.name(), valueClass));
        }
        nullEntry = new NodeVariableObject<>("null_entry", valueClass);
    }

    public void putAll(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void putAll(V value) {
        nullEntry.set(value);
        for (K key : keyClass.getEnumConstants()) {
            put(key, value);
        }
    }

    public void put(K key, V value) {
        if (key == null) {
            nullEntry.set(value);
        } else {
            IVariableNodeObject<V> node = variableMap.get(key);
            if (node == null) {
                throw new IllegalArgumentException("Unknown enum key " + key + " for " + key.getClass());
            }
            node.set(value);
        }
    }

    public NodeVariableObject<V> get(K key) {
        if (key == null) {
            return nullEntry;
        } else {
            NodeVariableObject<V> node = variableMap.get(key);
            if (node == null) {
                throw new IllegalArgumentException("Unknown enum key " + key + " for " + key.getClass());
            }
            return node;
        }
    }

    @Override
    public INodeObject<V> getNode(INodeStack stack) throws InvalidExpressionException {
        return new Node(stack.popObject(keyClass));
    }

    @Override
    public Class<V> getType() {
        return valueClass;
    }

    private class Node implements INodeObject<V>, IDependantNode {

        private final INodeObject<K> input;

        public Node(INodeObject<K> input) {
            this.input = input;
        }

        @Override
        public void visitDependants(IDependancyVisitor visitor) {
            visitor.dependOn((IExpressionNode) nullEntry);
            visitor.dependOnNodes(variableMap.values());
        }

        @Override
        public V evaluate() {
            return NodeFuncMap.this.get(input.evaluate()).evaluate();
        }

        @Override
        public Class<V> getType() {
            return valueClass;
        }

        @Override
        public INodeObject<V> inline() {
            return NodeInliningHelper.tryInline(this, input, Node::new, i -> {
                assert i instanceof IConstantNode;
                K key = i.evaluate();
                return NodeFuncMap.this.get(key).inline();
            });
        }
    }
}
