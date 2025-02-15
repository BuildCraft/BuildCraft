/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import java.util.Objects;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.StringFunctionPenta;
import buildcraft.lib.expression.node.func.NodeFuncBase;
import buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncBooleanBooleanBooleanBooleanToBoolean extends NodeFuncBase implements INodeFuncBoolean {

    public final IFuncBooleanBooleanBooleanBooleanToBoolean function;
    private final StringFunctionPenta stringFunction;

    public NodeFuncBooleanBooleanBooleanBooleanToBoolean(String name, IFuncBooleanBooleanBooleanBooleanToBoolean function) {
        this(function, (a, b, c, d) -> "[ boolean, boolean, boolean, boolean -> boolean ] " + name + "(" + a + ", " + b + ", " + c + ", " + d +  ")");
    }

    public NodeFuncBooleanBooleanBooleanBooleanToBoolean(IFuncBooleanBooleanBooleanBooleanToBoolean function, StringFunctionPenta stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}", "{D}");
    }

    @Override
    public NodeFuncBooleanBooleanBooleanBooleanToBoolean setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean d = stack.popBoolean();
        INodeBoolean c = stack.popBoolean();
        INodeBoolean b = stack.popBoolean();
        INodeBoolean a = stack.popBoolean();

        return create(a, b, c, d);
    }

    /** Shortcut to create a new {@link FuncBooleanBooleanBooleanBooleanToBoolean} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncBooleanBooleanBooleanBooleanToBoolean create(INodeBoolean argA, INodeBoolean argB, INodeBoolean argC, INodeBoolean argD) {
        return new FuncBooleanBooleanBooleanBooleanToBoolean(argA, argB, argC, argD); 
    }

    public class FuncBooleanBooleanBooleanBooleanToBoolean implements INodeBoolean, IDependantNode, IFunctionNode {
        public final INodeBoolean argA;
        public final INodeBoolean argB;
        public final INodeBoolean argC;
        public final INodeBoolean argD;

        public FuncBooleanBooleanBooleanBooleanToBoolean(INodeBoolean argA, INodeBoolean argB, INodeBoolean argC, INodeBoolean argD) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;
            this.argD = argD;

        }

        @Override
        public boolean evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate(), argD.evaluate());
        }

        @Override
        public INodeBoolean inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC, argD,
                    (a, b, c, d) -> new FuncBooleanBooleanBooleanBooleanToBoolean(a, b, c, d),
                    (a, b, c, d) -> new FuncBooleanBooleanBooleanBooleanToBoolean(a, b, c, d)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC, argD,
                (a, b, c, d) -> new FuncBooleanBooleanBooleanBooleanToBoolean(a, b, c, d),
                (a, b, c, d) -> NodeConstantBoolean.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate()))
            );
        }

        @Override
        public void visitDependants(IDependancyVisitor visitor) {
            if (!canInline) {
                if (function instanceof IDependantNode) {
                    visitor.dependOn((IDependantNode) function);
                } else {
                    visitor.dependOnExplictly(this);
                }
            }
            visitor.dependOn(argA, argB, argC, argD);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString(), argD.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncBooleanBooleanBooleanBooleanToBoolean.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA, argB, argC, argD);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncBooleanBooleanBooleanBooleanToBoolean other = (FuncBooleanBooleanBooleanBooleanToBoolean) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC) //
            &&Objects.equals(argD, other.argD);
        }
    }

    @FunctionalInterface
    public interface IFuncBooleanBooleanBooleanBooleanToBoolean {
        boolean apply(boolean a, boolean b, boolean c, boolean d);
    }
}
