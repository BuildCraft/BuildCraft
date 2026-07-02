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
import ct.buildcraft.lib.expression.node.func.StringFunctionBi;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase;
import ct.buildcraft.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncDoubleToObject<R> extends NodeFuncBase implements INodeFuncObject<R> {

    public final IFuncDoubleToObject<R> function;
    private final StringFunctionBi stringFunction;
    private final Class<R> returnType;

    public NodeFuncDoubleToObject(String name, Class<R> returnType, IFuncDoubleToObject<R> function) {
        this(returnType, function, (a) -> "[ double -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a +  ")");
    }

    public NodeFuncDoubleToObject(Class<R> returnType, IFuncDoubleToObject<R> function, StringFunctionBi stringFunction) {
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
        return stringFunction.apply("{A}");
    }

    @Override
    public NodeFuncDoubleToObject<R> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeDouble a = stack.popDouble();

        return create(a);
    }

    /** Shortcut to create a new {@link FuncDoubleToObject} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncDoubleToObject create(INodeDouble argA) {
        return new FuncDoubleToObject(argA); 
    }

    public class FuncDoubleToObject implements INodeObject<R>, IDependantNode, IFunctionNode {
        public final INodeDouble argA;

        public FuncDoubleToObject(INodeDouble argA) {
            this.argA = argA;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA,
                    (a) -> new FuncDoubleToObject(a),
                    (a) -> new FuncDoubleToObject(a)
                );
            }
            return NodeInliningHelper.tryInline(this, argA,
                (a) -> new FuncDoubleToObject(a),
                (a) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate()))
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
            return NodeFuncDoubleToObject.this;
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
            FuncDoubleToObject other = (FuncDoubleToObject) obj;
            return Objects.equals(argA, other.argA);
        }
    }

    @FunctionalInterface
    public interface IFuncDoubleToObject<R> {
        R apply(double a);
    }
}
