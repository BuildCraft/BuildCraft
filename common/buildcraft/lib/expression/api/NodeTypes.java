package buildcraft.lib.expression.api;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.cast.NodeCasting;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public class NodeTypes {

    public static final NodeType2<String> STRING = new NodeType2<>("");

    public static final Map<String, Class<?>> knownTypes = new HashMap<>();

    /** All of the OBJECT types. Unlike {@link #knownTypes} that this doesn't include long, double, or boolean */
    public static final Map<Class<?>, NodeType2<?>> objectTypes = new HashMap<>();

    static {
        knownTypes.put("long", long.class);
        knownTypes.put("double", double.class);
        knownTypes.put("boolean", boolean.class);

        addType("string", STRING);

        STRING.put_t_l("length", String::length);
        STRING.put_t_t("toLowerCase", a -> a.toLowerCase(Locale.ROOT));
        STRING.put_t_t("toUpperCase", a -> a.toUpperCase(Locale.ROOT));
    }

    public static Class<?> parseType(String type) throws InvalidExpressionException {
        Class<?> clazz = knownTypes.get(type.toLowerCase(Locale.ROOT));
        if (clazz != null) {
            return clazz;
        }
        throw new InvalidExpressionException("Unknown type " + clazz + ", must be one of " + knownTypes.keySet());
    }

    public static <T> NodeType2<T> getType(Class<T> clazz) {
        return (NodeType2<T>) objectTypes.get(clazz);
    }

    public static <T> void addType(String key, NodeType2<T> type) {
        knownTypes.put(key, type.type);
        objectTypes.put(type.type, type);
    }

    public static Class<?> getType(IExpressionNode node) {
        if (node instanceof INodeObject<?>) {
            return ((INodeObject<?>) node).getType();
        } else if (node instanceof INodeLong) return long.class;
        else if (node instanceof INodeDouble) return double.class;
        else if (node instanceof INodeBoolean) return boolean.class;
        else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }

    public static IVariableNode makeVariableNode(Class<?> type, String name) {
        if (type == long.class) return new NodeVariableLong(name);
        if (type == double.class) return new NodeVariableDouble(name);
        if (type == boolean.class) return new NodeVariableBoolean(name);
        return new NodeVariableObject<>(name, type);
    }

    public static IConstantNode createConstantNode(IExpressionNode node) {
        if (node instanceof INodeLong) return new NodeConstantLong(((INodeLong) node).evaluate());
        else if (node instanceof INodeDouble) return new NodeConstantDouble(((INodeDouble) node).evaluate());
        else if (node instanceof INodeBoolean) return NodeConstantBoolean.get(((INodeBoolean) node).evaluate());
        else if (node instanceof INodeObject) {
            INodeObject<?> nodeObj = (INodeObject<?>) node;
            return createConstantObject(nodeObj);
        } else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }

    private static <T> IConstantNode createConstantObject(INodeObject<T> nodeObj) {
        return new NodeConstantObject<>(nodeObj.getType(), nodeObj.evaluate());
    }

    public static IExpressionNode cast(IExpressionNode node, Class<?> to) throws InvalidExpressionException {
        if (to == double.class) return NodeCasting.castToDouble(node);
        if (to == String.class) return NodeCasting.castToString(node);
        if (to == long.class) {
            if (node instanceof INodeLong) {
                return node;
            } else {
                throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a long");
            }
        }
        if (to == boolean.class) {
            if (node instanceof INodeBoolean) {
                return node;
            } else {
                throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a boolean");
            }
        }
        throw new IllegalStateException("Unknown node type '" + to + "'");
    }
}
