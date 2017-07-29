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
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.StringFunctionTri;
import buildcraft.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectObjectToLong<A, B> implements INodeFuncLong {

    public final IFuncObjectObjectToLong<A, B> function;
    private final StringFunctionTri stringFunction;
    private final Class<A> argTypeA;
    private final Class<B> argTypeB;

    public NodeFuncObjectObjectToLong(String name, Class<A> argTypeA, Class<B> argTypeB, IFuncObjectObjectToLong<A, B> function) {
        this(argTypeA, argTypeB, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", " + NodeTypes.getName(argTypeB) + " -> long ] " + name + "(" + a + ", " + b +  ")", function);
    }

    public NodeFuncObjectObjectToLong(Class<A> argTypeA, Class<B> argTypeB, StringFunctionTri stringFunction, IFuncObjectObjectToLong<A, B> function) {
        this.argTypeA = argTypeA;
        this.argTypeB = argTypeB;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}");
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeObject<B> b = stack.popObject(argTypeB);
        INodeObject<A> a = stack.popObject(argTypeA);

        return new Func(a, b);
    }

    private class Func implements INodeLong {
        private final INodeObject<A> argA;
        private final INodeObject<B> argB;

        public Func(INodeObject<A> argA, INodeObject<B> argB) {
            this.argA = argA;
            this.argB = argB;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate());
        }

        @Override
        public INodeLong inline() {
            return NodeInliningHelper.tryInline(this, argA, argB, (a, b) -> new Func(a, b),
                    (a, b) -> NodeConstantLong.of(function.apply(a.evaluate(), b.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncObjectObjectToLong<A, B> {
        long apply(A a, B b);
    }
}
