/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject.IFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject.IFuncLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong.IFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToObject.IFuncObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean.IFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong.IFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject.IFuncObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncToBoolean.IFuncToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncToDouble;
import buildcraft.lib.expression.node.func.NodeFuncToDouble.IFuncToDouble;
import buildcraft.lib.expression.node.func.NodeFuncToLong;
import buildcraft.lib.expression.node.func.NodeFuncToLong.IFuncToLong;
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
    public static final String FUNCTION_ARG_SEPARATOR = "@";

    public final String name;
    private final FunctionContext[] parents;
    private final Map<String, IExpressionNode> variables = new HashMap<>();
    private final Map<String, Map<List<Class<?>>, INodeFunc>> functions = new HashMap<>();

    @Deprecated
    public FunctionContext() {
        this("");
    }

    @Deprecated
    public FunctionContext(FunctionContext parent) {
        this("", parent);
    }

    @Deprecated
    public FunctionContext(FunctionContext... parents) {
        this("", parents);
    }

    /** Creates a function context with no parents. You probably DON'T want this, as it doesn't have any of the useful
     * functions found in {@link DefaultContexts} */
    public FunctionContext(String name) {
        this.name = name;
        this.parents = new FunctionContext[0];
    }

    /** Constructs a function context that will delegate to the parent to find functions and variables if they don't
     * exist in this context. */
    public FunctionContext(String name, FunctionContext parent) {
        this.name = name;
        this.parents = new FunctionContext[] { parent };
    }

    /** Constructs a function context that will delegate to the parents, in order, to find functions and variables if
     * they don't exist in this context. */
    public FunctionContext(String name, FunctionContext... parents) {
        this.name = name;
        this.parents = parents.clone();
    }

    public FunctionContext[] getParents() {
        return parents;
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
        INodeFunc func = getFunction(name, Collections.emptyList());
        if (func != null) {
            try {
                return func.getNode(new NodeStack());
            } catch (InvalidExpressionException e) {
                throw new IllegalStateException("Found a 0-args function that didn't allow us to get a node for it!",
                    e);
            }
        }
        return null;
    }

    public boolean hasLocalVariable(String name) {
        name = name.toLowerCase(Locale.ROOT);
        return variables.containsKey(name);
    }

    public <E extends IExpressionNode> E putVariable(String name, E node) {
        name = name.toLowerCase(Locale.ROOT);
        if (NodeTypes.getType(name) != null) {
            throw new IllegalArgumentException("Cannot add a variable that clashes with a type! (Name = '" + name
                + "', Types = " + NodeTypes.getValidTypeNames() + ")");
        }
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
        putVariable(name, new INodeLong() {
            @Override
            public String toString() {
                return name + " = " + value + "L";
            }

            @Override
            public long evaluate() {
                return value;
            }

            @Override
            public INodeLong inline() {
                return new NodeConstantLong(value);
            }
        });
    }

    public void putConstantDouble(String name, double value) {
        putVariable(name, new INodeDouble() {
            @Override
            public String toString() {
                return name + " = " + value + "D";
            }

            @Override
            public double evaluate() {
                return value;
            }

            @Override
            public INodeDouble inline() {
                return new NodeConstantDouble(value);
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
                return name + " = " + value;
            }
        });
    }

    public <T> void putConstant(String name, Class<T> type, T value) {
        putVariable(name, new INodeObject<T>() {
            @Override
            public T evaluate() {
                return value;
            }

            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public INodeObject<T> inline() {
                return new NodeConstantObject<>(type, value);
            }

            @Override
            public String toString() {
                return name + " = " + value;
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
            putConstant(name, String.class, value);
        }
    }

    public Set<String> getAllVariables() {
        return variables.keySet();
    }

    // Function getter/setters

    public INodeFunc getFunction(String name, List<Class<?>> args) {
        Map<List<Class<?>>, INodeFunc> map = functions.get(name);
        if (map != null) {
            INodeFunc func = map.get(args);
            if (func != null) {
                return func;
            }
        }
        for (FunctionContext parent : parents) {
            INodeFunc func = parent.getFunction(name, args);
            if (func != null) return func;
        }
        return null;
    }

    public Map<List<Class<?>>, INodeFunc> getFunctions(String name) {
        name = name.toLowerCase(Locale.ROOT);
        Map<List<Class<?>>, INodeFunc> map = new HashMap<>();
        getFunctions0(name, map);
        return map;
    }

    public Map<String, Map<List<Class<?>>, INodeFunc>> getAllFunctions() {
        return functions;
    }

    private void getFunctions0(String name, Map<List<Class<?>>, INodeFunc> map) {
        for (FunctionContext parent : parents) {
            parent.getFunctions0(name, map);
        }
        Map<List<Class<?>>, INodeFunc> all = functions.get(name);
        if (all != null) {
            map.putAll(all);
        }
    }

    private static List<Class<?>> getArgTypes(INodeFunc function) {
        NodeStackRecording recorder = new NodeStackRecording();
        try {
            function.getNode(recorder);
        } catch (InvalidExpressionException e) {
            throw new IllegalStateException("This should never happen!", e);
        }
        List<Class<?>> types = new ArrayList<>(recorder.types);
        Collections.reverse(types);
        return types;
    }

    @Override
    public <F extends INodeFunc> F putFunction(String name, F function) {
        name = name.toLowerCase(Locale.ROOT);
        Map<List<Class<?>>, INodeFunc> map = functions.computeIfAbsent(name, k -> new HashMap<>());
        map.put(getArgTypes(function), function);
        return function;
    }

    // Various putFunction_in_out methods that make adding a function quicker

    public INodeFuncLong put_l(String name, IFuncToLong func) {
        return putFunction(name, new NodeFuncToLong(name, func));
    }

    public INodeFuncDouble put_d(String name, IFuncToDouble func) {
        return putFunction(name, new NodeFuncToDouble(name, func));
    }

    public INodeFuncBoolean put_b(String name, IFuncToBoolean func) {
        return putFunction(name, new NodeFuncToBoolean(name, func));
    }

    public INodeFuncObject<String> put_s(String name, Supplier<String> func) {
        return put_o(name, String.class, func);
    }

    public <T> INodeFuncObject<T> put_o(String name, Class<T> type, Supplier<T> func) {
        return putFunction(name, new NodeFuncToObject<>(name, type, func));
    }

    public INodeFuncObject<String> put_l_s(String name, IFuncLongToObject<String> func) {
        return put_l_o(name, String.class, func);
    }

    public INodeFuncObject<String> put_d_s(String name, IFuncDoubleToObject<String> func) {
        return put_d_o(name, String.class, func);
    }

    public INodeFuncBoolean put_s_b(String name, IFuncObjectToBoolean<String> func) {
        return put_o_b(name, String.class, func);
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

    public <A> INodeFuncObject<String> put_o_s(String name, Class<A> argA, IFuncObjectToObject<A, String> func) {
        return put_o_o(name, argA, String.class, func);
    }

    public INodeFuncObject<String> put_s_s(String name, IFuncObjectToObject<String, String> func) {
        return put_o_o(name, String.class, String.class, func);
    }

    public INodeFuncObject<String> put_ss_s(String name, IFuncObjectObjectToObject<String, String, String> func) {
        return put_oo_o(name, String.class, String.class, String.class, func);
    }
}
