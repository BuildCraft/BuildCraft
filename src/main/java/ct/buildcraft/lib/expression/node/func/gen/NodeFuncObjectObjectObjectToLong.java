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
import ct.buildcraft.lib.expression.node.func.StringFunctionQuad;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectObjectObjectToLong<A, B, C> extends NodeFuncBase implements INodeFuncLong {

    public final IFuncObjectObjectObjectToLong<A, B, C> function;
    private final StringFunctionQuad stringFunction;
    private final Class<A> argTypeA;
    private final Class<B> argTypeB;
    private final Class<C> argTypeC;

    public NodeFuncObjectObjectObjectToLong(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToLong<A, B, C> function) {
        this(argTypeA, argTypeB, argTypeC, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", " + NodeTypes.getName(argTypeB) + ", " + NodeTypes.getName(argTypeC) + " -> long ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncObjectObjectObjectToLong(Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, IFuncObjectObjectObjectToLong<A, B, C> function, StringFunctionQuad stringFunction) {
        this.argTypeA = argTypeA;
        this.argTypeB = argTypeB;
        this.argTypeC = argTypeC;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public NodeFuncObjectObjectObjectToLong<A, B, C> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeObject<C> c = stack.popObject(argTypeC);
        INodeObject<B> b = stack.popObject(argTypeB);
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b, c);
    }

    /** Shortcut to create a new {@link FuncObjectObjectObjectToLong} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectObjectObjectToLong create(INodeObject<A> argA, INodeObject<B> argB, INodeObject<C> argC) {
        return new FuncObjectObjectObjectToLong(argA, argB, argC); 
    }

    public class FuncObjectObjectObjectToLong implements INodeLong, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeObject<B> argB;
        public final INodeObject<C> argC;

        public FuncObjectObjectObjectToLong(INodeObject<A> argA, INodeObject<B> argB, INodeObject<C> argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeLong inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    (a, b, c) -> new FuncObjectObjectObjectToLong(a, b, c),
                    (a, b, c) -> new FuncObjectObjectObjectToLong(a, b, c)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                (a, b, c) -> new FuncObjectObjectObjectToLong(a, b, c),
                (a, b, c) -> NodeConstantLong.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
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
            return NodeFuncObjectObjectObjectToLong.this;
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
            FuncObjectObjectObjectToLong other = (FuncObjectObjectObjectToLong) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectObjectObjectToLong<A, B, C> {
        long apply(A a, B b, C c);
    }
}
