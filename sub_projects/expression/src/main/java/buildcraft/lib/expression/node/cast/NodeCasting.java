/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.NodeStack;
import buildcraft.lib.expression.api.*;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;

import java.util.Collections;

public class NodeCasting {
    public static INodeObject<String> castToString(IExpressionNode node) {
        if (node instanceof INodeObject) {
            if (((INodeObject<?>) node).getType() == String.class) {
                return (INodeObject<String>) node;
            }
        }
        return new NodeCastToString(node);
    }

    public static INodeFuncObject<String> castToString(INodeFunc func) {
        if (func instanceof INodeFuncObject) {
            if (((INodeFuncObject<?>) func).getType() == String.class) {
                return (INodeFuncObject<String>) func;
            }
        }
        return new INodeFuncObject<String>() {
            @Override
            public Class<String> getType() {
                return String.class;
            }

            @Override
            public INodeObject<String> getNode(INodeStack stack) throws InvalidExpressionException {
                return new NodeCastToString(func.getNode(stack));
            }
        };
    }

    public static INodeDouble castToDouble(IExpressionNode node) throws InvalidExpressionException {
        if (node instanceof INodeDouble) {
            return (INodeDouble) node;
        }
        Class<?> type = NodeTypes.getType(node);
        FunctionContext ctx = NodeTypes.getContext(type);
        if (ctx == null) {
            throw new InvalidExpressionException("Cannot cast " + node + " to a double!");
        }
        INodeFunc func = ctx.getFunction("(double)", Collections.singletonList(type));
        if (func == null || NodeTypes.getType(func) != double.class) {
            throw new InvalidExpressionException("Cannot cast " + node + " to a double!");
        }
        return (INodeDouble) func.getNode(new NodeStack(node));
    }

    public static INodeFuncDouble castToDouble(INodeFunc func) throws InvalidExpressionException {
        if (func instanceof INodeFuncDouble) {
            return (INodeFuncDouble) func;
        }
        Class<?> type = NodeTypes.getType(func);
        FunctionContext ctx = NodeTypes.getContext(type);
        if (ctx == null) {
            throw new InvalidExpressionException("Cannot cast " + func + " to a double!");
        }
        INodeFunc caster = ctx.getFunction("(double)", Collections.singletonList(type));
        if (caster == null || NodeTypes.getType(caster) != double.class) {
            throw new InvalidExpressionException("Cannot cast " + func + " to a double!");
        }
        return (stack) -> (INodeDouble) caster.getNode(new NodeStack(func.getNode(stack)));
    }

    public static IExpressionNode castToType(IExpressionNode node, Class<?> to) throws InvalidExpressionException {
        Class<?> from = NodeTypes.getType(node);
        if (from == to) {
            return node;
        }
        FunctionContext castingContext = new FunctionContext(NodeTypes.getContext(from), NodeTypes.getContext(to));
        INodeFunc caster =
            castingContext.getFunction("(" + NodeTypes.getName(to) + ")", Collections.singletonList(from));
        if (caster == null) {
            if (to == String.class) {
                return new NodeCastToString(node);
            }
            throw new InvalidExpressionException(
                "Cannot cast from " + NodeTypes.getName(from) + " to " + NodeTypes.getName(to));
        }
        NodeStack stack = new NodeStack(node);
        stack.setRecorder(Collections.singletonList(from), caster);
        IExpressionNode casted = caster.getNode(stack);
        stack.checkAndRemoveRecorder();
        Class<?> actual = NodeTypes.getType(casted);
        if (actual != to) {
            throw new IllegalStateException("The caster " + caster + " didn't produce the correct result! (Expected "
                + to + ", but got " + actual + ")");
        }
        return casted;
    }

    public static <T> INodeObject<T> castToObject(IExpressionNode node, Class<T> clazz)
        throws InvalidExpressionException {
        return (INodeObject<T>) castToType(node, clazz);
    }

    public static <T> INodeFuncObject<T> castToObject(INodeFunc func, Class<T> to) throws InvalidExpressionException {
        Class<?> from = NodeTypes.getType(func);
        if (from == to) {
            return (INodeFuncObject<T>) func;
        }
        FunctionContext castingContext = new FunctionContext(NodeTypes.getContext(from), NodeTypes.getContext(to));
        INodeFunc caster =
            castingContext.getFunction("(" + NodeTypes.getName(to) + ")", Collections.singletonList(from));
        if (caster == null) {
            if (to == String.class) {
                return (INodeFuncObject<T>) castToString(func);
            }
            throw new InvalidExpressionException(
                "Cannot cast from " + NodeTypes.getName(from) + " to " + NodeTypes.getName(to));
        }
        return new INodeFuncObject<T>() {
            @Override
            public INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException {
                return (INodeObject<T>) caster.getNode(new NodeStack(func.getNode(stack)));
            }

            @Override
            public Class<T> getType() {
                return to;
            }
        };
    }
}
