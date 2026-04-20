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
import ct.buildcraft.lib.expression.node.func.StringFunctionQuad;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectObjectBooleanToObject<A, B, R> extends NodeFuncBase implements INodeFuncObject<R> {

    public final IFuncObjectObjectBooleanToObject<A, B, R> function;
    private final StringFunctionQuad stringFunction;
    private final Class<A> argTypeA;
    private final Class<B> argTypeB;
    private final Class<R> returnType;

    public NodeFuncObjectObjectBooleanToObject(String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectBooleanToObject<A, B, R> function) {
        this(argTypeA, argTypeB, returnType, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", " + NodeTypes.getName(argTypeB) + ", boolean -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncObjectObjectBooleanToObject(Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, IFuncObjectObjectBooleanToObject<A, B, R> function, StringFunctionQuad stringFunction) {
        this.argTypeA = argTypeA;
        this.argTypeB = argTypeB;
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
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public NodeFuncObjectObjectBooleanToObject<A, B, R> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeBoolean c = stack.popBoolean();
        INodeObject<B> b = stack.popObject(argTypeB);
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b, c);
    }

    /** Shortcut to create a new {@link FuncObjectObjectBooleanToObject} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectObjectBooleanToObject create(INodeObject<A> argA, INodeObject<B> argB, INodeBoolean argC) {
        return new FuncObjectObjectBooleanToObject(argA, argB, argC); 
    }

    public class FuncObjectObjectBooleanToObject implements INodeObject<R>, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeObject<B> argB;
        public final INodeBoolean argC;

        public FuncObjectObjectBooleanToObject(INodeObject<A> argA, INodeObject<B> argB, INodeBoolean argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    (a, b, c) -> new FuncObjectObjectBooleanToObject(a, b, c),
                    (a, b, c) -> new FuncObjectObjectBooleanToObject(a, b, c)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                (a, b, c) -> new FuncObjectObjectBooleanToObject(a, b, c),
                (a, b, c) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
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
            return NodeFuncObjectObjectBooleanToObject.this;
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
            FuncObjectObjectBooleanToObject other = (FuncObjectObjectBooleanToObject) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectObjectBooleanToObject<A, B, R> {
        R apply(A a, B b, boolean c);
    }
}
