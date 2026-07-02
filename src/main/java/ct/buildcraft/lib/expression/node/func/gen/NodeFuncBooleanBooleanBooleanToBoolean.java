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
import ct.buildcraft.lib.expression.node.func.StringFunctionQuad;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncBooleanBooleanBooleanToBoolean extends NodeFuncBase implements INodeFuncBoolean {

    public final IFuncBooleanBooleanBooleanToBoolean function;
    private final StringFunctionQuad stringFunction;

    public NodeFuncBooleanBooleanBooleanToBoolean(String name, IFuncBooleanBooleanBooleanToBoolean function) {
        this(function, (a, b, c) -> "[ boolean, boolean, boolean -> boolean ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncBooleanBooleanBooleanToBoolean(IFuncBooleanBooleanBooleanToBoolean function, StringFunctionQuad stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public NodeFuncBooleanBooleanBooleanToBoolean setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean c = stack.popBoolean();
        INodeBoolean b = stack.popBoolean();
        INodeBoolean a = stack.popBoolean();

        return create(a, b, c);
    }

    /** Shortcut to create a new {@link FuncBooleanBooleanBooleanToBoolean} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncBooleanBooleanBooleanToBoolean create(INodeBoolean argA, INodeBoolean argB, INodeBoolean argC) {
        return new FuncBooleanBooleanBooleanToBoolean(argA, argB, argC); 
    }

    public class FuncBooleanBooleanBooleanToBoolean implements INodeBoolean, IDependantNode, IFunctionNode {
        public final INodeBoolean argA;
        public final INodeBoolean argB;
        public final INodeBoolean argC;

        public FuncBooleanBooleanBooleanToBoolean(INodeBoolean argA, INodeBoolean argB, INodeBoolean argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public boolean evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeBoolean inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    (a, b, c) -> new FuncBooleanBooleanBooleanToBoolean(a, b, c),
                    (a, b, c) -> new FuncBooleanBooleanBooleanToBoolean(a, b, c)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                (a, b, c) -> new FuncBooleanBooleanBooleanToBoolean(a, b, c),
                (a, b, c) -> NodeConstantBoolean.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
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
            visitor.dependOn(argA, argB, argC);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncBooleanBooleanBooleanToBoolean.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA, argB, argC);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncBooleanBooleanBooleanToBoolean other = (FuncBooleanBooleanBooleanToBoolean) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC);
        }
    }

    @FunctionalInterface
    public interface IFuncBooleanBooleanBooleanToBoolean {
        boolean apply(boolean a, boolean b, boolean c);
    }
}
