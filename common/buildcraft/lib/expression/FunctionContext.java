package buildcraft.lib.expression;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncString;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.func.*;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToDouble.IFuncDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToDouble.IFuncDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToLong.IFuncDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong.IFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong.IFuncLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncStringToLong.IFuncStringToLong;
import buildcraft.lib.expression.node.func.NodeFuncToBoolean.IFuncToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncToString.IFuncToString;
import buildcraft.lib.expression.node.value.*;

public class FunctionContext {
    public static final String FUNCTION_ARG_SEPERATOR = "^";

    private final FunctionContext[] parents;
    private final Map<String, IExpressionNode> variables = new HashMap<>();
    private final Map<String, INodeFunc> functions = new HashMap<>();

    /** Creates a function context with no parents. You probably DON'T want this, as it doesn't have any of the useful
     * functions found in {@link DefaultContexts} */
    public FunctionContext() {
        this.parents = new FunctionContext[0];
    }

    /** Constructs a function context that will delegate to the parent to find functions and variables if they don't
     * exist in this context. */
    public FunctionContext(FunctionContext parent) {
        this.parents = new FunctionContext[] { parent };
    }

    /** Constructs a function context that will delegate to the parents, in order, to find functions and variables if
     * they don't exist in this context. */
    public FunctionContext(FunctionContext[] parents) {
        this.parents = parents.clone();
    }

    // Variable getter/setters

    public IExpressionNode getVariable(String name) {
        name = name.toLowerCase(Locale.ROOT);
        IExpressionNode current = variables.get(name);
        if (current != null) {
            return current;
        }
        for (FunctionContext parent : parents) {
            IExpressionNode node = parent.getVariable(name);
            if (node != null) return node;
        }
        return null;
    }

    public boolean hasLocalVariable(String name) {
        name = name.toLowerCase(Locale.ROOT);
        return variables.containsKey(name);
    }

    public <E extends IExpressionNode> E putVariable(String name, E node) {
        name = name.toLowerCase(Locale.ROOT);
        variables.put(name, node);
        return node;
    }

    public IVariableNode putVariable(String name, NodeType type) {
        switch (type) {
            case BOOLEAN:
                return putVariableBoolean(name);
            case DOUBLE:
                return putVariableDouble(name);
            case LONG:
                return putVariableLong(name);
            case STRING:
                return putVariableString(name);
            default:
                throw new IllegalArgumentException("Unknown node type " + type);
        }
    }

    public NodeVariableLong putVariableLong(String name) {
        NodeVariableLong node = new NodeVariableLong(name);
        return putVariable(name, node);
    }

    public NodeVariableDouble putVariableDouble(String name) {
        NodeVariableDouble node = new NodeVariableDouble(name);
        return putVariable(name, node);
    }

    public NodeVariableBoolean putVariableBoolean(String name) {
        NodeVariableBoolean node = new NodeVariableBoolean(name);
        return putVariable(name, node);
    }

    public NodeVariableString putVariableString(String name) {
        NodeVariableString node = new NodeVariableString(name);
        return putVariable(name, node);
    }

    public void putConstantLong(String name, long value) {
        putVariable(name, new NodeConstantLong(value) {
            @Override
            public String toString() {
                return name;
            }
        });
    }

    public void putConstantDouble(String name, double value) {
        putVariable(name, new NodeConstantDouble(value) {
            @Override
            public String toString() {
                return name;
            }
        });
    }

    public void putConstantBoolean(String name, boolean value) {
        putVariable(name, new INodeBoolean() {
            @Override
            public boolean evaluate() {
                return value;
            }

            @Override
            public INodeBoolean inline() {
                return NodeConstantBoolean.get(value);
            }

            @Override
            public String toString() {
                return name;
            }
        });
    }

    public void putConstantString(String name, String value) {
        putVariable(name, new NodeConstantString(value) {
            @Override
            public String toString() {
                return name;
            }
        });
    }

    public void putParsedConstant(String name, String value) {
        if (InternalCompiler.isValidLong(value)) {
            putConstantLong(name, InternalCompiler.parseValidLong(value));
        } else if (InternalCompiler.isValidDouble(value)) {
            putConstantDouble(name, Double.parseDouble(value));
        } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            putConstantBoolean(name, "true".equalsIgnoreCase(value));
        } else {
            putConstantString(name, value);
        }
    }

    // Function getter/setters

    public INodeFunc getFunction(String name, int args) {
        name = name.toLowerCase(Locale.ROOT);
        return getFunction0(name + FUNCTION_ARG_SEPERATOR + args);
    }

    private INodeFunc getFunction0(String fullName) {
        INodeFunc current = functions.get(fullName);
        if (current != null) {
            return current;
        }
        for (FunctionContext parent : parents) {
            INodeFunc func = parent.getFunction0(fullName);
            if (func != null) return func;
        }
        return null;
    }

    private static int getArgCount(INodeFunc function) {
        NodeStackRecording recorder = new NodeStackRecording();
        try {
            function.getNode(recorder);
        } catch (InvalidExpressionException e) {
            throw new IllegalStateException("This should never happen!", e);
        }
        return recorder.types.size();
    }

    public <F extends INodeFunc> F putFunction(String name, F function) {
        name = name.toLowerCase(Locale.ROOT);
        functions.put(name + FUNCTION_ARG_SEPERATOR + getArgCount(function), function);
        return function;
    }

    // Various putFunction_in_out methods that make adding a function quicker

    public INodeFuncBoolean put_b(String name, IFuncToBoolean func) {
        return putFunction(name, new NodeFuncToBoolean(name, func));
    }

    public INodeFuncString put_s(String name, IFuncToString func) {
        return putFunction(name, new NodeFuncToString(name, func));
    }

    public INodeFuncLong put_l_l(String name, IFuncLongToLong func) {
        return putFunction(name, new NodeFuncLongToLong(func, (a) -> name + "(" + a + ")"));
    }

    public INodeFuncLong put_ll_l(String name, IFuncLongLongToLong func) {
        return putFunction(name, new NodeFuncLongLongToLong(func, (a, b) -> name + "(" + a + ", " + b + ")"));
    }

    public INodeFuncLong put_d_l(String name, IFuncDoubleToLong func) {
        return putFunction(name, new NodeFuncDoubleToLong(func, (a) -> name + "(" + a + ")"));
    }

    public INodeFuncDouble put_d_d(String name, IFuncDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleToDouble(func, (a) -> name + "(" + a + ")"));
    }

    public INodeFuncDouble put_dd_d(String name, IFuncDoubleDoubleToDouble func) {
        return putFunction(name, new NodeFuncDoubleDoubleToDouble(func, (a, b) -> name + "(" + a + ", " + b + ")"));
    }

    public INodeFuncLong put_s_l(String name, IFuncStringToLong func) {
        return putFunction(name, new NodeFuncStringToLong(func, (a) -> name + "(" + a + ")"));
    }
}
