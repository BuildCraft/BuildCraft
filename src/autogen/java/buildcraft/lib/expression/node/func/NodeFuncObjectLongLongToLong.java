/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.StringFunctionQuad;
import buildcraft.lib.expression.node.func.NodeFuncBase;
import buildcraft.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectLongLongToLong<A> extends NodeFuncBase implements INodeFuncLong {

    public final IFuncObjectLongLongToLong<A> function;
    private final StringFunctionQuad stringFunction;
    private final Class<A> argTypeA;

    public NodeFuncObjectLongLongToLong(String name, Class<A> argTypeA, IFuncObjectLongLongToLong<A> function) {
        this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", long, long -> long ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncObjectLongLongToLong(Class<A> argTypeA, IFuncObjectLongLongToLong<A> function, StringFunctionQuad stringFunction) {
        this.argTypeA = argTypeA;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong c = stack.popLong();
        INodeLong b = stack.popLong();
        INodeObject<A> a = stack.popObject(argTypeA);

        return new Func(a, b, c);
    }

    private class Func implements INodeLong {
        private final INodeObject<A> argA;
        private final INodeLong argB;
        private final INodeLong argC;

        public Func(INodeObject<A> argA, INodeLong argB, INodeLong argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeLong inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                        Func::new,
                        Func::new
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    Func::new,
                (a, b, c) -> NodeConstantLong.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncObjectLongLongToLong<A> {
        long apply(A a, long b, long c);
    }
}
