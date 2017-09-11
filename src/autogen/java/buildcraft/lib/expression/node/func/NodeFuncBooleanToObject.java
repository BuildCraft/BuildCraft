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
import buildcraft.lib.expression.node.func.StringFunctionBi;
import buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncBooleanToObject<R> implements INodeFuncObject<R> {

    public final IFuncBooleanToObject<R> function;
    private final StringFunctionBi stringFunction;
    private final Class<R> returnType;

    public NodeFuncBooleanToObject(String name, Class<R> returnType, IFuncBooleanToObject<R> function) {
        this(returnType, function, (a) -> "[ boolean -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a +  ")");
    }

    public NodeFuncBooleanToObject(Class<R> returnType, IFuncBooleanToObject<R> function, StringFunctionBi stringFunction) {
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
        return stringFunction.apply("{A}");
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean a = stack.popBoolean();

        return new Func(a);
    }

    private class Func implements INodeObject<R> {
        private final INodeBoolean argA;

        public Func(INodeBoolean argA) {
            this.argA = argA;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            return NodeInliningHelper.tryInline(this, argA, (a) -> new Func(a),
                    (a) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncBooleanToObject<R> {
        R apply(boolean a);
    }
}
