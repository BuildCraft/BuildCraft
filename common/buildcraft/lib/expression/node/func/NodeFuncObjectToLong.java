/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeFuncObjectToLong<T> implements INodeFuncLong {

    public final Class<T> argType;
    public final IFuncObjectToLong<T> function;
    private final StringFunctionBi stringFunction;

    public NodeFuncObjectToLong(Class<T> argType, IFuncObjectToLong<T> function) {
        this.argType = argType;
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncObjectToLong(Class<T> argType, IFuncObjectToLong<T> function, String fnString) {
        this(argType, function, (a) -> "[" + a + "] " + fnString);
    }

    public NodeFuncObjectToLong(Class<T> argType, IFuncObjectToLong<T> function, StringFunctionBi stringFunction) {
        this.argType = argType;
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[string -> long] {" + function.toString() + "}" : stringFunction.apply("{0}");
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        return new Func<>(stack.popObject(argType), function, stringFunction);
    }

    private static class Func<T> implements INodeLong {
        private final INodeObject<T> arg;
        private final IFuncObjectToLong<T> function;
        private final StringFunctionBi stringFunction;

        public Func(INodeObject<T>  arg, IFuncObjectToLong<T> function, StringFunctionBi stringFunction) {
            this.arg = arg;
            this.function = function;
            this.stringFunction = stringFunction;
        }

        @Override
        public long evaluate() {
            return function.apply(arg.evaluate());
        }

        @Override
        public INodeLong inline() {
            return NodeInliningHelper.tryInline(this, arg, (a) -> new Func<>(a, function, stringFunction),//
                (a) -> new NodeConstantLong(function.apply(a.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + arg + " -> long] {" + function.toString() + "}"//
                : stringFunction.apply(arg.toString());
        }
    }

    public interface IFuncObjectToLong<T> {
        long apply(T arg);
    }
}
