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
import ct.buildcraft.lib.expression.node.func.StringFunctionPenta;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncDoubleDoubleDoubleDoubleToLong extends NodeFuncBase implements INodeFuncLong {

    public final IFuncDoubleDoubleDoubleDoubleToLong function;
    private final StringFunctionPenta stringFunction;

    public NodeFuncDoubleDoubleDoubleDoubleToLong(String name, IFuncDoubleDoubleDoubleDoubleToLong function) {
        this(function, (a, b, c, d) -> "[ double, double, double, double -> long ] " + name + "(" + a + ", " + b + ", " + c + ", " + d +  ")");
    }

    public NodeFuncDoubleDoubleDoubleDoubleToLong(IFuncDoubleDoubleDoubleDoubleToLong function, StringFunctionPenta stringFunction) {

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}", "{D}");
    }

    @Override
    public NodeFuncDoubleDoubleDoubleDoubleToLong setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeDouble d = stack.popDouble();
        INodeDouble c = stack.popDouble();
        INodeDouble b = stack.popDouble();
        INodeDouble a = stack.popDouble();

        return create(a, b, c, d);
    }

    /** Shortcut to create a new {@link FuncDoubleDoubleDoubleDoubleToLong} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncDoubleDoubleDoubleDoubleToLong create(INodeDouble argA, INodeDouble argB, INodeDouble argC, INodeDouble argD) {
        return new FuncDoubleDoubleDoubleDoubleToLong(argA, argB, argC, argD); 
    }

    public class FuncDoubleDoubleDoubleDoubleToLong implements INodeLong, IDependantNode, IFunctionNode {
        public final INodeDouble argA;
        public final INodeDouble argB;
        public final INodeDouble argC;
        public final INodeDouble argD;

        public FuncDoubleDoubleDoubleDoubleToLong(INodeDouble argA, INodeDouble argB, INodeDouble argC, INodeDouble argD) {
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
                    (a, b, c, d) -> new FuncDoubleDoubleDoubleDoubleToLong(a, b, c, d),
                    (a, b, c, d) -> new FuncDoubleDoubleDoubleDoubleToLong(a, b, c, d)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC, argD,
                (a, b, c, d) -> new FuncDoubleDoubleDoubleDoubleToLong(a, b, c, d),
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
            return NodeFuncDoubleDoubleDoubleDoubleToLong.this;
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
            FuncDoubleDoubleDoubleDoubleToLong other = (FuncDoubleDoubleDoubleDoubleToLong) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC) //
            &&Objects.equals(argD, other.argD);
        }
    }

    @FunctionalInterface
    public interface IFuncDoubleDoubleDoubleDoubleToLong {
        long apply(double a, double b, double c, double d);
    }
}
