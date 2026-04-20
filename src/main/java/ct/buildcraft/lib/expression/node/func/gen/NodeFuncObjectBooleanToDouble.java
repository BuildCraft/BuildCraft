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
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.func.StringFunctionTri;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantDouble;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectBooleanToDouble<A> extends NodeFuncBase implements INodeFuncDouble {

    public final IFuncObjectBooleanToDouble<A> function;
    private final StringFunctionTri stringFunction;
    private final Class<A> argTypeA;

    public NodeFuncObjectBooleanToDouble(String name, Class<A> argTypeA, IFuncObjectBooleanToDouble<A> function) {
        this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", boolean -> double ] " + name + "(" + a + ", " + b +  ")");
    }

    public NodeFuncObjectBooleanToDouble(Class<A> argTypeA, IFuncObjectBooleanToDouble<A> function, StringFunctionTri stringFunction) {
        this.argTypeA = argTypeA;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}");
    }

    @Override
    public NodeFuncObjectBooleanToDouble<A> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean b = stack.popBoolean();
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b);
    }

    /** Shortcut to create a new {@link FuncObjectBooleanToDouble} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectBooleanToDouble create(INodeObject<A> argA, INodeBoolean argB) {
        return new FuncObjectBooleanToDouble(argA, argB); 
    }

    public class FuncObjectBooleanToDouble implements INodeDouble, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeBoolean argB;

        public FuncObjectBooleanToDouble(INodeObject<A> argA, INodeBoolean argB) {
            this.argA = argA;
            this.argB = argB;

        }

        @Override
        public double evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate());
        }

        @Override
        public INodeDouble inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB,
                    (a, b) -> new FuncObjectBooleanToDouble(a, b),
                    (a, b) -> new FuncObjectBooleanToDouble(a, b)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB,
                (a, b) -> new FuncObjectBooleanToDouble(a, b),
                (a, b) -> NodeConstantDouble.of(function.apply(a.evaluate(), b.evaluate()))
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
            return NodeFuncObjectBooleanToDouble.this;
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
            FuncObjectBooleanToDouble other = (FuncObjectBooleanToDouble) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectBooleanToDouble<A> {
        double apply(A a, boolean b);
    }
}
