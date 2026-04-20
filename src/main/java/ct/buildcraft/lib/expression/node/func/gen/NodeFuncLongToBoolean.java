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
import ct.buildcraft.lib.expression.node.func.StringFunctionBi;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncLongToBoolean extends NodeFuncBase implements INodeFuncBoolean {

    public final IFuncLongToBoolean function;
    private final StringFunctionBi stringFunction;

    public NodeFuncLongToBoolean(String name, IFuncLongToBoolean function) {
        this(function, (a) -> "[ long -> boolean ] " + name + "(" + a +  ")");
    }

    public NodeFuncLongToBoolean(IFuncLongToBoolean function, StringFunctionBi stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}");
    }

    @Override
    public NodeFuncLongToBoolean setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong a = stack.popLong();

        return create(a);
    }

    /** Shortcut to create a new {@link FuncLongToBoolean} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncLongToBoolean create(INodeLong argA) {
        return new FuncLongToBoolean(argA); 
    }

    public class FuncLongToBoolean implements INodeBoolean, IDependantNode, IFunctionNode {
        public final INodeLong argA;

        public FuncLongToBoolean(INodeLong argA) {
            this.argA = argA;

        }

        @Override
        public boolean evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeBoolean inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA,
                    (a) -> new FuncLongToBoolean(a),
                    (a) -> new FuncLongToBoolean(a)
                );
            }
            return NodeInliningHelper.tryInline(this, argA,
                (a) -> new FuncLongToBoolean(a),
                (a) -> NodeConstantBoolean.of(function.apply(a.evaluate()))
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
            visitor.dependOn(argA);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncLongToBoolean.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncLongToBoolean other = (FuncLongToBoolean) obj;
            return Objects.equals(argA, other.argA);
        }
    }

    @FunctionalInterface
    public interface IFuncLongToBoolean {
        boolean apply(long a);
    }
}
