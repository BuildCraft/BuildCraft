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
import buildcraft.lib.expression.node.func.StringFunctionBi;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectToBoolean<A> implements INodeFuncBoolean {

    public final IFuncObjectToBoolean<A> function;
    private final StringFunctionBi stringFunction;
    private final Class<A> argTypeA;

    public NodeFuncObjectToBoolean(String name, Class<A> argTypeA, IFuncObjectToBoolean<A> function) {
        this(argTypeA, (a) -> name + "(" + a +  ")", function);
    }

    public NodeFuncObjectToBoolean(Class<A> argTypeA, StringFunctionBi stringFunction, IFuncObjectToBoolean<A> function) {
        this.argTypeA = argTypeA;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}");
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeObject<A> a = stack.popObject(argTypeA);

        return new Func(a);
    }

    private class Func implements INodeBoolean {
        private final INodeObject<A> argA;

        public Func(INodeObject<A> argA) {
            this.argA = argA;

        }

        @Override
        public boolean evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeBoolean inline() {
            return NodeInliningHelper.tryInline(this, argA, (a) -> new Func(a),
                    (a) -> NodeConstantBoolean.of(function.apply(a.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncObjectToBoolean<A> {
        boolean apply(A a);
    }
}
