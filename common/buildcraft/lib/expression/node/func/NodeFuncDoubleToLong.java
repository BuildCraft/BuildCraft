/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeFuncDoubleToLong implements INodeFuncLong {

    public final IFuncDoubleToLong function;
    private final StringFunctionBi stringFunction;

    public NodeFuncDoubleToLong(IFuncDoubleToLong function) {
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncDoubleToLong(IFuncDoubleToLong function, String fnString) {
        this(function, (a) -> "[" + a + "] " + fnString);
    }

    public NodeFuncDoubleToLong(IFuncDoubleToLong function, StringFunctionBi stringFunction) {
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[double -> long] {" + function.toString() + "}" : stringFunction.apply("{0}");
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        return new Func(stack.popDouble(), function, stringFunction);
    }

    private static class Func implements INodeLong {
        private final INodeDouble arg;
        private final IFuncDoubleToLong function;
        private final StringFunctionBi stringFunction;

        public Func(INodeDouble arg, IFuncDoubleToLong function, StringFunctionBi stringFunction) {
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
            return NodeInliningHelper.tryInline(this, arg, (a) -> new Func(a, function, stringFunction),//
                    (a) -> new NodeConstantLong(function.apply(a.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + arg + " -> long] {" + function.toString() + "}"//
                : stringFunction.apply(arg.toString());
        }
    }

    public interface IFuncDoubleToLong {
        long apply(double arg);
    }
}
