/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.func.StringFunctionTri;
import buildcraft.lib.expression.node.func.NodeFuncBase;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncBooleanBooleanToBoolean extends NodeFuncBase implements INodeFuncBoolean {

    public final IFuncBooleanBooleanToBoolean function;
    private final StringFunctionTri stringFunction;

    public NodeFuncBooleanBooleanToBoolean(String name, IFuncBooleanBooleanToBoolean function) {
        this(function, (a, b) -> "[ boolean, boolean -> boolean ] " + name + "(" + a + ", " + b +  ")");
    }

    public NodeFuncBooleanBooleanToBoolean(IFuncBooleanBooleanToBoolean function, StringFunctionTri stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}");
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean b = stack.popBoolean();
        INodeBoolean a = stack.popBoolean();

        return new Func(a, b);
    }

    private class Func implements INodeBoolean {
        private final INodeBoolean argA;
        private final INodeBoolean argB;

        public Func(INodeBoolean argA, INodeBoolean argB) {
            this.argA = argA;
            this.argB = argB;

        }

        @Override
        public boolean evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate());
        }

        @Override
        public INodeBoolean inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB,
                        Func::new,
                        Func::new
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB,
                    Func::new,
                (a, b) -> NodeConstantBoolean.of(function.apply(a.evaluate(), b.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncBooleanBooleanToBoolean {
        boolean apply(boolean a, boolean b);
    }
}
