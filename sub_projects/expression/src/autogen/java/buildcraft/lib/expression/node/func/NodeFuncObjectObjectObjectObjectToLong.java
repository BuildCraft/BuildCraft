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
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.StringFunctionPenta;
import buildcraft.lib.expression.node.func.NodeFuncBase;
import buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import buildcraft.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectObjectObjectObjectToLong<A, B, C, D> extends NodeFuncBase implements INodeFuncLong {

    public final IFuncObjectObjectObjectObjectToLong<A, B, C, D> function;
    private final StringFunctionPenta stringFunction;
    private final Class<A> argTypeA;
    private final Class<B> argTypeB;
    private final Class<C> argTypeC;
    private final Class<D> argTypeD;

    public NodeFuncObjectObjectObjectObjectToLong(String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, B, C, D> function) {
        this(argTypeA, argTypeB, argTypeC, argTypeD, function, (a, b, c, d) -> "[ " + NodeTypes.getName(argTypeA) + ", " + NodeTypes.getName(argTypeB) + ", " + NodeTypes.getName(argTypeC) + ", " + NodeTypes.getName(argTypeD) + " -> long ] " + name + "(" + a + ", " + b + ", " + c + ", " + d +  ")");
    }

    public NodeFuncObjectObjectObjectObjectToLong(Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, Class<D> argTypeD, IFuncObjectObjectObjectObjectToLong<A, B, C, D> function, StringFunctionPenta stringFunction) {
        this.argTypeA = argTypeA;
        this.argTypeB = argTypeB;
        this.argTypeC = argTypeC;
        this.argTypeD = argTypeD;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}", "{D}");
    }

    @Override
    public NodeFuncObjectObjectObjectObjectToLong<A, B, C, D> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeObject<D> d = stack.popObject(argTypeD);
        INodeObject<C> c = stack.popObject(argTypeC);
        INodeObject<B> b = stack.popObject(argTypeB);
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b, c, d);
    }

    /** Shortcut to create a new {@link FuncObjectObjectObjectObjectToLong} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectObjectObjectObjectToLong create(INodeObject<A> argA, INodeObject<B> argB, INodeObject<C> argC, INodeObject<D> argD) {
        return new FuncObjectObjectObjectObjectToLong(argA, argB, argC, argD); 
    }

    public class FuncObjectObjectObjectObjectToLong implements INodeLong, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeObject<B> argB;
        public final INodeObject<C> argC;
        public final INodeObject<D> argD;

        public FuncObjectObjectObjectObjectToLong(INodeObject<A> argA, INodeObject<B> argB, INodeObject<C> argC, INodeObject<D> argD) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;
            this.argD = argD;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate(), argD.evaluate());
        }

        @Override
        public INodeLong inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC, argD,
                    (a, b, c, d) -> new FuncObjectObjectObjectObjectToLong(a, b, c, d),
                    (a, b, c, d) -> new FuncObjectObjectObjectObjectToLong(a, b, c, d)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC, argD,
                (a, b, c, d) -> new FuncObjectObjectObjectObjectToLong(a, b, c, d),
                (a, b, c, d) -> NodeConstantLong.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate()))
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
            return NodeFuncObjectObjectObjectObjectToLong.this;
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
            FuncObjectObjectObjectObjectToLong other = (FuncObjectObjectObjectObjectToLong) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC) //
            &&Objects.equals(argD, other.argD);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectObjectObjectObjectToLong<A, B, C, D> {
        long apply(A a, B b, C c, D d);
    }
}
