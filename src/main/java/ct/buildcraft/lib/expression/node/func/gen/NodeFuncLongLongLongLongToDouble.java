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
import ct.buildcraft.lib.expression.node.func.StringFunctionPenta;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantDouble;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncLongLongLongLongToDouble extends NodeFuncBase implements INodeFuncDouble {

    public final IFuncLongLongLongLongToDouble function;
    private final StringFunctionPenta stringFunction;

    public NodeFuncLongLongLongLongToDouble(String name, IFuncLongLongLongLongToDouble function) {
        this(function, (a, b, c, d) -> "[ long, long, long, long -> double ] " + name + "(" + a + ", " + b + ", " + c + ", " + d +  ")");
    }

    public NodeFuncLongLongLongLongToDouble(IFuncLongLongLongLongToDouble function, StringFunctionPenta stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}", "{D}");
    }

    @Override
    public NodeFuncLongLongLongLongToDouble setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong d = stack.popLong();
        INodeLong c = stack.popLong();
        INodeLong b = stack.popLong();
        INodeLong a = stack.popLong();

        return create(a, b, c, d);
    }

    /** Shortcut to create a new {@link FuncLongLongLongLongToDouble} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncLongLongLongLongToDouble create(INodeLong argA, INodeLong argB, INodeLong argC, INodeLong argD) {
        return new FuncLongLongLongLongToDouble(argA, argB, argC, argD); 
    }

    public class FuncLongLongLongLongToDouble implements INodeDouble, IDependantNode, IFunctionNode {
        public final INodeLong argA;
        public final INodeLong argB;
        public final INodeLong argC;
        public final INodeLong argD;

        public FuncLongLongLongLongToDouble(INodeLong argA, INodeLong argB, INodeLong argC, INodeLong argD) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;
            this.argD = argD;

        }

        @Override
        public double evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate(), argD.evaluate());
        }

        @Override
        public INodeDouble inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC, argD,
                    (a, b, c, d) -> new FuncLongLongLongLongToDouble(a, b, c, d),
                    (a, b, c, d) -> new FuncLongLongLongLongToDouble(a, b, c, d)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC, argD,
                (a, b, c, d) -> new FuncLongLongLongLongToDouble(a, b, c, d),
                (a, b, c, d) -> NodeConstantDouble.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate()))
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
            return NodeFuncLongLongLongLongToDouble.this;
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
            FuncLongLongLongLongToDouble other = (FuncLongLongLongLongToDouble) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC) //
            &&Objects.equals(argD, other.argD);
        }
    }

    @FunctionalInterface
    public interface IFuncLongLongLongLongToDouble {
        double apply(long a, long b, long c, long d);
    }
}
