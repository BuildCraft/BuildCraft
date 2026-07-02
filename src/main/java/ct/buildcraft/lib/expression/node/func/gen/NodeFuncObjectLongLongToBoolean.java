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
public class NodeFuncObjectLongLongToBoolean<A> extends NodeFuncBase implements INodeFuncBoolean {

    public final IFuncObjectLongLongToBoolean<A> function;
    private final StringFunctionQuad stringFunction;
    private final Class<A> argTypeA;

    public NodeFuncObjectLongLongToBoolean(String name, Class<A> argTypeA, IFuncObjectLongLongToBoolean<A> function) {
        this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", long, long -> boolean ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncObjectLongLongToBoolean(Class<A> argTypeA, IFuncObjectLongLongToBoolean<A> function, StringFunctionQuad stringFunction) {
        this.argTypeA = argTypeA;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public NodeFuncObjectLongLongToBoolean<A> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong c = stack.popLong();
        INodeLong b = stack.popLong();
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b, c);
    }

    /** Shortcut to create a new {@link FuncObjectLongLongToBoolean} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectLongLongToBoolean create(INodeObject<A> argA, INodeLong argB, INodeLong argC) {
        return new FuncObjectLongLongToBoolean(argA, argB, argC); 
    }

    public class FuncObjectLongLongToBoolean implements INodeBoolean, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeLong argB;
        public final INodeLong argC;

        public FuncObjectLongLongToBoolean(INodeObject<A> argA, INodeLong argB, INodeLong argC) {
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
                    (a, b, c) -> new FuncObjectLongLongToBoolean(a, b, c),
                    (a, b, c) -> new FuncObjectLongLongToBoolean(a, b, c)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                (a, b, c) -> new FuncObjectLongLongToBoolean(a, b, c),
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
            return NodeFuncObjectLongLongToBoolean.this;
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
            FuncObjectLongLongToBoolean other = (FuncObjectLongLongToBoolean) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectLongLongToBoolean<A> {
        boolean apply(A a, long b, long c);
    }
}
