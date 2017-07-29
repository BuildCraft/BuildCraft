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
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.StringFunctionTri;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncLongLongToBoolean implements INodeFuncBoolean {

    public final IFuncLongLongToBoolean function;
    private final StringFunctionTri stringFunction;

    public NodeFuncLongLongToBoolean(String name, IFuncLongLongToBoolean function) {
        this((a, b) -> "[ long, long -> boolean ] " + name + "(" + a + b +  ")", function);
    }

    public NodeFuncLongLongToBoolean(StringFunctionTri stringFunction, IFuncLongLongToBoolean function) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}");
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong b = stack.popLong();
        INodeLong a = stack.popLong();

        return new Func(a, b);
    }

    private class Func implements INodeBoolean {
        private final INodeLong argA;
        private final INodeLong argB;

        public Func(INodeLong argA, INodeLong argB) {
            this.argA = argA;
            this.argB = argB;

        }

        @Override
        public boolean evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate());
        }

        @Override
        public INodeBoolean inline() {
            return NodeInliningHelper.tryInline(this, argA, argB, (a, b) -> new Func(a, b),
                    (a, b) -> NodeConstantBoolean.of(function.apply(a.evaluate(), b.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncLongLongToBoolean {
        boolean apply(long a, long b);
    }
}
