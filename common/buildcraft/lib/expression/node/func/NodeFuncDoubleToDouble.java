/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeFuncDoubleToDouble implements INodeFuncDouble {

    public final IFuncDoubleToDouble function;
    private final StringFunctionBi stringFunction;

    public NodeFuncDoubleToDouble(IFuncDoubleToDouble function) {
        this.function = function;
        stringFunction = null;
    }

    public NodeFuncDoubleToDouble(IFuncDoubleToDouble function, String fnString) {
        this(new IFuncDoubleToDouble() {
            @Override
            public double apply(double arg) {
                return function.apply(arg);
            }

            @Override
            public String toString() {
                return fnString;
            }
        });
    }

    public NodeFuncDoubleToDouble(IFuncDoubleToDouble function, StringFunctionBi stringFunction) {
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[double -> double] {" + function.toString() + "}" : stringFunction.apply("{0}");
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
        return new Func(stack.popDouble(), function, stringFunction);
    }

    private static class Func implements INodeDouble {
        private final INodeDouble arg;
        private final IFuncDoubleToDouble function;
        private final StringFunctionBi stringFunction;

        public Func(INodeDouble arg, IFuncDoubleToDouble function, StringFunctionBi stringFunction) {
            this.arg = arg;
            this.function = function;
            this.stringFunction = stringFunction;
        }

        @Override
        public double evaluate() {
            return function.apply(arg.evaluate());
        }

        @Override
        public INodeDouble inline() {
            return NodeInliningHelper.tryInline(this, arg, (a) -> new Func(a, function, stringFunction),//
                    (a) -> new NodeConstantDouble(function.apply(a.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + arg + " -> double] {" + function.toString() + "}"//
                : stringFunction.apply(arg.toString());
        }
    }

    public interface IFuncDoubleToDouble {
        double apply(double arg);
    }
}
