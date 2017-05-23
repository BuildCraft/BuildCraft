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

public class NodeFuncStringToLong implements INodeFuncLong {

    public final IFuncStringToLong function;
    private final StringFunctionBi stringFunction;

    public NodeFuncStringToLong(IFuncStringToLong function) {
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncStringToLong(IFuncStringToLong function, String fnString) {
        this(function, (a) -> "[" + a + "] " + fnString);
    }

    public NodeFuncStringToLong(IFuncStringToLong function, StringFunctionBi stringFunction) {
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[string -> long] {" + function.toString() + "}" : stringFunction.apply("{0}");
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        return new Func(stack.popString(), function, stringFunction);
    }

    private static class Func implements INodeLong {
        private final INodeString arg;
        private final IFuncStringToLong function;
        private final StringFunctionBi stringFunction;

        public Func(INodeString arg, IFuncStringToLong function, StringFunctionBi stringFunction) {
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

    public interface IFuncStringToLong {
        long apply(String arg);
    }
}
