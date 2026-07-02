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
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.func.StringFunctionBi;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncBooleanToLong extends NodeFuncBase implements INodeFuncLong {

    public final IFuncBooleanToLong function;
    private final StringFunctionBi stringFunction;

    public NodeFuncBooleanToLong(String name, IFuncBooleanToLong function) {
        this(function, (a) -> "[ boolean -> long ] " + name + "(" + a +  ")");
    }

    public NodeFuncBooleanToLong(IFuncBooleanToLong function, StringFunctionBi stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}");
    }

    @Override
    public NodeFuncBooleanToLong setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean a = stack.popBoolean();

        return create(a);
    }

    /** Shortcut to create a new {@link FuncBooleanToLong} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncBooleanToLong create(INodeBoolean argA) {
        return new FuncBooleanToLong(argA); 
    }

    public class FuncBooleanToLong implements INodeLong, IDependantNode, IFunctionNode {
        public final INodeBoolean argA;

        public FuncBooleanToLong(INodeBoolean argA) {
            this.argA = argA;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeLong inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA,
                    (a) -> new FuncBooleanToLong(a),
                    (a) -> new FuncBooleanToLong(a)
                );
            }
            return NodeInliningHelper.tryInline(this, argA,
                (a) -> new FuncBooleanToLong(a),
                (a) -> NodeConstantLong.of(function.apply(a.evaluate()))
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
            return NodeFuncBooleanToLong.this;
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
            FuncBooleanToLong other = (FuncBooleanToLong) obj;
            return Objects.equals(argA, other.argA);
        }
    }

    @FunctionalInterface
    public interface IFuncBooleanToLong {
        long apply(boolean a);
    }
}
