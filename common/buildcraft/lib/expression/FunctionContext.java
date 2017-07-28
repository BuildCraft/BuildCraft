/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToObject.IFuncObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong.IFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncToBoolean.IFuncToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncToObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public class FunctionContext extends FunctionContextBase {
    public static final String FUNCTION_ARG_SEPARATOR = "^";

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
    public FunctionContext(FunctionContext... parents) {
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

    public IVariableNode putVariable(String name, Class<?> type) {
        if (type == boolean.class) return putVariableBoolean(name);
        if (type == long.class) return putVariableLong(name);
        if (type == double.class) return putVariableDouble(name);
        return putVariableObject(name, type);
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

    public NodeVariableObject<String> putVariableString(String name) {
        return putVariableObject(name, String.class);
    }

    public <T> NodeVariableObject<T> putVariableObject(String name, Class<T> type) {
        NodeVariableObject<T> node = new NodeVariableObject<>(name, type);
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
                return NodeConstantBoolean.of(value);
            }

            @Override
            public String toString() {
                return name;
            }
        });
    }

    public <T> void putConstant(String name, Class<T> type, T value) {
        putVariable(name, new NodeConstantObject<>(type, value));
    }

    public void putParsedConstant(String name, String value) {
        if (InternalCompiler.isValidLong(value)) {
            putConstantLong(name, InternalCompiler.parseValidLong(value));
        } else if (InternalCompiler.isValidDouble(value)) {
            putConstantDouble(name, Double.parseDouble(value));
        } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            putConstantBoolean(name, "true".equalsIgnoreCase(value));
        } else {
            putConstant(name, String.class, value);
        }
    }

    // Function getter/setters

    public INodeFunc getFunction(String name, int args) {
        name = name.toLowerCase(Locale.ROOT);
        return getFunction0(name + FUNCTION_ARG_SEPARATOR + args);
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

    @Override
    public <F extends INodeFunc> F putFunction(String name, F function) {
        name = name.toLowerCase(Locale.ROOT);
        functions.put(name + FUNCTION_ARG_SEPARATOR + getArgCount(function), function);
        return function;
    }

    // Various putFunction_in_out methods that make adding a function quicker

    public INodeFuncBoolean put_b(String name, IFuncToBoolean func) {
        return putFunction(name, new NodeFuncToBoolean(name, func));
    }

    public INodeFuncObject<String> put_s(String name, Supplier<String> func) {
        return put_o(name, String.class, func);
    }

    public <T> INodeFuncObject<T> put_o(String name, Class<T> type, Supplier<T> func) {
        return putFunction(name, new NodeFuncToObject<>(name, type, func));
    }

    public INodeFuncLong put_s_l(String name, IFuncObjectToLong<String> func) {
        return put_o_l(name, String.class, func);
    }

    public INodeFuncLong put_sl_l(String name, IFuncObjectLongToLong<String> func) {
        return put_ol_l(name, String.class, func);
    }

    public INodeFuncLong put_sl_l(String name, IFuncObjectLongLongToLong<String> func) {
        return put_oll_l(name, String.class, func);
    }

    public INodeFuncObject<String> put_ss_s(String name, IFuncObjectObjectToObject<String, String, String> func) {
        return put_oo_o(name, String.class, String.class, String.class, func);
    }
}
