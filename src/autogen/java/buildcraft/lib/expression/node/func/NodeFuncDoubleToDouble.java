/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.func.StringFunctionBi;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncDoubleToDouble implements INodeFuncDouble {

    public final IFuncDoubleToDouble function;
    private final StringFunctionBi stringFunction;

    public NodeFuncDoubleToDouble(String name, IFuncDoubleToDouble function) {
        this((a) -> name + "(" + a +  ")", function);
    }

    public NodeFuncDoubleToDouble(StringFunctionBi stringFunction, IFuncDoubleToDouble function) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}");
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {

        INodeDouble a = stack.popDouble();

        return new Func(a);
    }

    private class Func implements INodeDouble {
        private final INodeDouble argA;

        public Func(INodeDouble argA) {
            this.argA = argA;

        }

        @Override
        public double evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeDouble inline() {
            return NodeInliningHelper.tryInline(this, argA, (a) -> new Func(a),
                    (a) -> NodeConstantDouble.of(function.apply(a.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncDoubleToDouble {
        double apply(double a);
    }
}
