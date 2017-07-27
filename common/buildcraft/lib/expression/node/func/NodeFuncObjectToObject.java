/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import java.util.function.Function;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeFuncObjectToObject<F, T> implements INodeFuncObject<T> {

    public final Class<F> argType;
    public final Class<T> returnType;
    public final Function<F, T> function;
    private final StringFunctionBi stringFunction;

    public NodeFuncObjectToObject(Class<F> argType, Class<T> returnType, Function<F, T> function) {
        this.argType = argType;
        this.returnType = returnType;
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncObjectToObject(Class<F> argType, Class<T> returnType, Function<F, T> function, String fnString) {
        this(argType, returnType, function, (a) -> "[" + a + "] " + fnString);
    }

    public NodeFuncObjectToObject(Class<F> argType, Class<T> returnType, Function<F, T> function,
        StringFunctionBi stringFunction) {
        this.argType = argType;
        this.returnType = returnType;
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public Class<T> getType() {
        return returnType;
    }

    @Override
    public String toString() {
        if (stringFunction != null) {
            return stringFunction.apply("{0}");
        }
        return "[" + argType.getSimpleName() + " -> " + returnType.getSimpleName() + "] {" + function.toString() + "}";
    }

    @Override
    public INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException {
        return new Func(stack.popObject(argType));
    }

    private class Func implements INodeObject<T> {
        private final INodeObject<F> arg;

        public Func(INodeObject<F> arg) {
            this.arg = arg;
        }

        @Override
        public Class<T> getType() {
            return returnType;
        }

        @Override
        public T evaluate() {
            return function.apply(arg.evaluate());
        }

        @Override
        public INodeObject<T> inline() {
            return NodeInliningHelper.tryInline(this, arg, (a) -> new Func(a),//
                (a) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + arg + " -> " + returnType.getSimpleName() + "] {" + function.toString() + "}"//
                : stringFunction.apply(arg.toString());
        }
    }
}
