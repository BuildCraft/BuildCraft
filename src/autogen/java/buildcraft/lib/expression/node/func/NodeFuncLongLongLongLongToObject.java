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
import buildcraft.lib.expression.node.func.StringFunctionPenta;
import buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncLongLongLongLongToObject<R> implements INodeFuncObject<R> {

    public final IFuncLongLongLongLongToObject<R> function;
    private final StringFunctionPenta stringFunction;
    private final Class<R> returnType;

    public NodeFuncLongLongLongLongToObject(String name, Class<R> returnType, IFuncLongLongLongLongToObject<R> function) {
        this(returnType, (a, b, c, d) -> "[ long, long, long, long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c + ", " + d +  ")", function);
    }

    public NodeFuncLongLongLongLongToObject(Class<R> returnType, StringFunctionPenta stringFunction, IFuncLongLongLongLongToObject<R> function) {
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

        INodeLong d = stack.popLong();
        INodeLong c = stack.popLong();
        INodeLong b = stack.popLong();
        INodeLong a = stack.popLong();

        return new Func(a, b, c, d);
    }

    private class Func implements INodeObject<R> {
        private final INodeLong argA;
        private final INodeLong argB;
        private final INodeLong argC;
        private final INodeLong argD;

        public Func(INodeLong argA, INodeLong argB, INodeLong argC, INodeLong argD) {
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
    public interface IFuncLongLongLongLongToObject<R> {
        R apply(long a, long b, long c, long d);
    }
}
