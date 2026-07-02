/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.func.gen;

import java.util.Objects;

import ct.buildcraft.lib.expression.NodeInliningHelper;
import ct.buildcraft.lib.expression.api.IDependantNode;
import ct.buildcraft.lib.expression.api.IDependancyVisitor;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.func.StringFunctionTri;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncDoubleDoubleToBoolean extends NodeFuncBase implements INodeFuncBoolean {

    public final IFuncDoubleDoubleToBoolean function;
    private final StringFunctionTri stringFunction;

    public NodeFuncDoubleDoubleToBoolean(String name, IFuncDoubleDoubleToBoolean function) {
        this(function, (a, b) -> "[ double, double -> boolean ] " + name + "(" + a + ", " + b +  ")");
    }

    public NodeFuncDoubleDoubleToBoolean(IFuncDoubleDoubleToBoolean function, StringFunctionTri stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}");
    }

    @Override
    public NodeFuncDoubleDoubleToBoolean setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeDouble b = stack.popDouble();
        INodeDouble a = stack.popDouble();

        return create(a, b);
    }

    /** Shortcut to create a new {@link FuncDoubleDoubleToBoolean} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncDoubleDoubleToBoolean create(INodeDouble argA, INodeDouble argB) {
        return new FuncDoubleDoubleToBoolean(argA, argB); 
    }

    public class FuncDoubleDoubleToBoolean implements INodeBoolean, IDependantNode, IFunctionNode {
        public final INodeDouble argA;
        public final INodeDouble argB;

        public FuncDoubleDoubleToBoolean(INodeDouble argA, INodeDouble argB) {
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
                    (a, b) -> new FuncDoubleDoubleToBoolean(a, b),
                    (a, b) -> new FuncDoubleDoubleToBoolean(a, b)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB,
                (a, b) -> new FuncDoubleDoubleToBoolean(a, b),
                (a, b) -> NodeConstantBoolean.of(function.apply(a.evaluate(), b.evaluate()))
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
            visitor.dependOn(argA, argB);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncDoubleDoubleToBoolean.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA, argB);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncDoubleDoubleToBoolean other = (FuncDoubleDoubleToBoolean) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB);
        }
    }

    @FunctionalInterface
    public interface IFuncDoubleDoubleToBoolean {
        boolean apply(double a, double b);
    }
}
