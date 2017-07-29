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
public class NodeFuncDoubleDoubleDoubleToObject<R> implements INodeFuncObject<R> {

    public final IFuncDoubleDoubleDoubleToObject<R> function;
    private final StringFunctionQuad stringFunction;
    private final Class<R> returnType;

    public NodeFuncDoubleDoubleDoubleToObject(String name, Class<R> returnType, IFuncDoubleDoubleDoubleToObject<R> function) {
        this(returnType, (a, b, c) -> "[ double, double, double -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c +  ")", function);
    }

    public NodeFuncDoubleDoubleDoubleToObject(Class<R> returnType, StringFunctionQuad stringFunction, IFuncDoubleDoubleDoubleToObject<R> function) {
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

        INodeDouble c = stack.popDouble();
        INodeDouble b = stack.popDouble();
        INodeDouble a = stack.popDouble();

        return new Func(a, b, c);
    }

    private class Func implements INodeObject<R> {
        private final INodeDouble argA;
        private final INodeDouble argB;
        private final INodeDouble argC;

        public Func(INodeDouble argA, INodeDouble argB, INodeDouble argC) {
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
    public interface IFuncDoubleDoubleDoubleToObject<R> {
        R apply(double a, double b, double c);
    }
}
