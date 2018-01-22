/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.func.StringFunctionQuad;
import buildcraft.lib.expression.node.func.NodeFuncBase;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncDoubleDoubleDoubleToDouble extends NodeFuncBase implements INodeFuncDouble {

    public final IFuncDoubleDoubleDoubleToDouble function;
    private final StringFunctionQuad stringFunction;

    public NodeFuncDoubleDoubleDoubleToDouble(String name, IFuncDoubleDoubleDoubleToDouble function) {
        this(function, (a, b, c) -> "[ double, double, double -> double ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncDoubleDoubleDoubleToDouble(IFuncDoubleDoubleDoubleToDouble function, StringFunctionQuad stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {

        INodeDouble c = stack.popDouble();
        INodeDouble b = stack.popDouble();
        INodeDouble a = stack.popDouble();

        return new Func(a, b, c);
    }

    private class Func implements INodeDouble {
        private final INodeDouble argA;
        private final INodeDouble argB;
        private final INodeDouble argC;

        public Func(INodeDouble argA, INodeDouble argB, INodeDouble argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public double evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeDouble inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                        Func::new,
                        Func::new
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    Func::new,
                (a, b, c) -> NodeConstantDouble.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString());
        }
    }

    @FunctionalInterface
    public interface IFuncDoubleDoubleDoubleToDouble {
        double apply(double a, double b, double c);
    }
}
