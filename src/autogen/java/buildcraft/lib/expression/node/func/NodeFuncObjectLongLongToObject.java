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
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.StringFunctionQuad;
import buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectLongLongToObject<A, R> implements INodeFuncObject<R> {

    public final IFuncObjectLongLongToObject<A, R> function;
    private final StringFunctionQuad stringFunction;
    private final Class<A> argTypeA;
    private final Class<R> returnType;

    public NodeFuncObjectLongLongToObject(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongLongToObject<A, R> function) {
        this(argTypeA, returnType, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", long, long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c +  ")", function);
    }

    public NodeFuncObjectLongLongToObject(Class<A> argTypeA, Class<R> returnType, StringFunctionQuad stringFunction, IFuncObjectLongLongToObject<A, R> function) {
        this.argTypeA = argTypeA;
        this.returnType = returnType;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public Class<R> getType() {
        return returnType;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong c = stack.popLong();
        INodeLong b = stack.popLong();
        INodeObject<A> a = stack.popObject(argTypeA);

        return new Func(a, b, c);
    }

    private class Func implements INodeObject<R> {
        private final INodeObject<A> argA;
        private final INodeLong argB;
        private final INodeLong argC;

        public Func(INodeObject<A> argA, INodeLong argB, INodeLong argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            return NodeInliningHelper.tryInline(this, argA, argB, argC, (a, b, c) -> new Func(a, b, c),
                    (a, b, c) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncObjectLongLongToObject<A, R> {
        R apply(A a, long b, long c);
    }
}
