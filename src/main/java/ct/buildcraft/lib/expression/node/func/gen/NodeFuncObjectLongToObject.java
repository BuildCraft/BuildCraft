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
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.func.StringFunctionTri;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectLongToObject<A, R> extends NodeFuncBase implements INodeFuncObject<R> {

    public final IFuncObjectLongToObject<A, R> function;
    private final StringFunctionTri stringFunction;
    private final Class<A> argTypeA;
    private final Class<R> returnType;

    public NodeFuncObjectLongToObject(String name, Class<A> argTypeA, Class<R> returnType, IFuncObjectLongToObject<A, R> function) {
        this(argTypeA, returnType, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b +  ")");
    }

    public NodeFuncObjectLongToObject(Class<A> argTypeA, Class<R> returnType, IFuncObjectLongToObject<A, R> function, StringFunctionTri stringFunction) {
        this.argTypeA = argTypeA;
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
        return stringFunction.apply("{A}", "{B}");
    }

    @Override
    public NodeFuncObjectLongToObject<A, R> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong b = stack.popLong();
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b);
    }

    /** Shortcut to create a new {@link FuncObjectLongToObject} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectLongToObject create(INodeObject<A> argA, INodeLong argB) {
        return new FuncObjectLongToObject(argA, argB); 
    }

    public class FuncObjectLongToObject implements INodeObject<R>, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeLong argB;

        public FuncObjectLongToObject(INodeObject<A> argA, INodeLong argB) {
            this.argA = argA;
            this.argB = argB;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB,
                    (a, b) -> new FuncObjectLongToObject(a, b),
                    (a, b) -> new FuncObjectLongToObject(a, b)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB,
                (a, b) -> new FuncObjectLongToObject(a, b),
                (a, b) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate(), b.evaluate()))
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
            return NodeFuncObjectLongToObject.this;
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
            FuncObjectLongToObject other = (FuncObjectLongToObject) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectLongToObject<A, R> {
        R apply(A a, long b);
    }
}
