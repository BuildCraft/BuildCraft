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
import buildcraft.lib.expression.node.func.StringFunctionPenta;
import buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, R> implements INodeFuncObject<R> {

    public final IFuncObjectObjectObjectObjectToObject<A, B, C, D, R> function;
    private final StringFunctionPenta stringFunction;
    private final Class<A> argTypeA;
    private final Class<B> argTypeB;
    private final Class<C> argTypeC;
    private final Class<D> argTypeD;
    private final Class<R> returnType;

    public NodeFuncObjectObjectObjectObjectToObject(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, IFuncObjectObjectObjectObjectToObject<A, B, C, D, R> function) {
        this(argTypeA, argTypeB, argTypeC, argTypeD, returnType, (a, b, c, d) -> name + "(" + a + b + c + d +  ")", function);
    }

    public NodeFuncObjectObjectObjectObjectToObject(Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, Class<R> returnType, StringFunctionPenta stringFunction, IFuncObjectObjectObjectObjectToObject<A, B, C, D, R> function) {
        this.argTypeA = argTypeA;
        this.argTypeB = argTypeB;
        this.argTypeC = argTypeC;
        this.argTypeD = argTypeD;
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
        return stringFunction.apply("{A}", "{B}", "{C}", "{D}");
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeObject<D> d = stack.popObject(argTypeD);
        INodeObject<C> c = stack.popObject(argTypeC);
        INodeObject<B> b = stack.popObject(argTypeB);
        INodeObject<A> a = stack.popObject(argTypeA);

        return new Func(a, b, c, d);
    }

    private class Func implements INodeObject<R> {
        private final INodeObject<A> argA;
        private final INodeObject<B> argB;
        private final INodeObject<C> argC;
        private final INodeObject<D> argD;

        public Func(INodeObject<A> argA, INodeObject<B> argB, INodeObject<C> argC, INodeObject<D> argD) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;
            this.argD = argD;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate(), argD.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            return NodeInliningHelper.tryInline(this, argA, argB, argC, argD, (a, b, c, d) -> new Func(a, b, c, d),
                    (a, b, c, d) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString(), argD.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncObjectObjectObjectObjectToObject<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}
