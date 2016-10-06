package buildcraft.lib.expression;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IFunctionContext;
import buildcraft.lib.expression.api.IFunctionMap;
import buildcraft.lib.expression.node.simple.NodeMutableBoolean;
import buildcraft.lib.expression.node.simple.NodeMutableDouble;
import buildcraft.lib.expression.node.simple.NodeMutableLong;
import buildcraft.lib.expression.node.simple.NodeMutableString;

/** Holds a set of function-local variables that can be called upon by the expression. */
public class FunctionContext implements IFunctionContext {
    private final IFunctionMap map;
    private final Map<String, IExpressionNode> allNodes = new HashMap<>();
    private final Map<String, NodeMutableBoolean> booleans = new HashMap<>();
    private final Map<String, NodeMutableDouble> doubles = new HashMap<>();
    private final Map<String, NodeMutableLong> longs = new HashMap<>();
    private final Map<String, NodeMutableString> strings = new HashMap<>();

    public FunctionContext(IFunctionMap map) {
        this.map = map;
    }

    public FunctionContext() {
        this(new FunctionMap());
    }

    public IFunctionMap getFunctionMap() {
        return map;
    }

    public NodeMutableBoolean getOrAddBoolean(String name) {
        return getOrAdd(name, booleans, new NodeMutableBoolean());
    }

    public NodeMutableBoolean getBoolean(String name) {
        return booleans.get(name);
    }

    public NodeMutableDouble getOrAddDouble(String name) {
        return getOrAdd(name, doubles, new NodeMutableDouble());
    }

    public NodeMutableBoolean getDouble(String name) {
        return booleans.get(name);
    }

    public NodeMutableLong getOrAddLong(String name) {
        return getOrAdd(name, longs, new NodeMutableLong());
    }

    public NodeMutableBoolean getLong(String name) {
        return booleans.get(name);
    }

    public NodeMutableString getOrAddString(String name) {
        return getOrAdd(name, strings, new NodeMutableString());
    }

    public NodeMutableBoolean getString(String name) {
        return booleans.get(name);
    }

    private <N extends IExpressionNode> N getOrAdd(String name, Map<String, N> toAddTo, N instance) {
        name = name.toLowerCase(Locale.ROOT);
        N existant = get(name);
        if (existant != null) {
            return existant;
        }
        toAddTo.put(name, instance);
        allNodes.put(name, instance);
        return instance;
    }

    public IExpressionNode getAny(String name) {
        return allNodes.get(name.toLowerCase(Locale.ROOT));
    }

    private <N extends IExpressionNode> N get(String name) {
        return (N) getAny(name);
    }
}
