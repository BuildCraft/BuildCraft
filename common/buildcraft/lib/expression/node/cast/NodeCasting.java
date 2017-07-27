/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.cast;

import java.util.function.Function;

import buildcraft.lib.expression.NodeStack;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType2;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject;

public class NodeCasting {
    public static INodeObject<String> castToString(IExpressionNode node) {
        if (node instanceof INodeObject) {
            if (((INodeObject<?>) node).getType() == String.class) {
                return (INodeObject<String>) node;
            }
        }
        return new NodeCastToString(node);
    }

    public static INodeDouble castToDouble(IExpressionNode node) throws InvalidExpressionException {
        if (node instanceof INodeDouble) {
            return (INodeDouble) node;
        }

        if (node instanceof INodeLong) {
            return new NodeCastLongToDouble((INodeLong) node);
        }

        throw new InvalidExpressionException("Cannot cast " + node + " to a double!");
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

    public static INodeFuncDouble castToDouble(INodeFunc func) throws InvalidExpressionException {
        if (func instanceof INodeFuncDouble) {
            return (INodeFuncDouble) func;
        }

        if (func instanceof INodeFuncLong) {
            final INodeFuncLong funcLong = (INodeFuncLong) func;
            return (stack) -> new NodeCastLongToDouble(funcLong.getNode(stack));
        }

        throw new InvalidExpressionException("Cannot cast " + func + " to a double!");
    }

    public static <T> INodeObject<T> castToObject(IExpressionNode node, Class<T> clazz)
        throws InvalidExpressionException {
        if (node instanceof INodeObject) {
            return castObjectToObject((INodeObject<?>) node, clazz);
        } else {
            throw new InvalidExpressionException("Cannot cast " + node + " to " + clazz.getSimpleName());
        }
    }

    public static <F, T> INodeObject<T> castObjectToObject(INodeObject<F> nodeFrom, Class<T> clazz)
        throws InvalidExpressionException {
        Class<F> fromClass = nodeFrom.getType();
        if (fromClass == clazz) {
            return (INodeObject<T>) nodeFrom;
        } else {
            NodeType2<T> typeTo = NodeTypes.getType(clazz);
            Function<F, T> caster = typeTo.getCast(fromClass);
            if (caster == null) {
                throw new InvalidExpressionException("Cannot cast " + nodeFrom + " to " + clazz.getSimpleName());
            }
            INodeStack stack = new NodeStack(nodeFrom);
            return new NodeFuncObjectToObject<>(fromClass, clazz, caster).getNode(stack);
        }
    }

    public static <T> INodeFuncObject<T> castToObject(INodeFunc node, Class<T> clazz)
        throws InvalidExpressionException {
        if (node instanceof INodeFuncObject) {
            return castObjectToObject((INodeFuncObject<?>) node, clazz);
        } else {
            throw new InvalidExpressionException("Cannot cast " + node + " to " + clazz.getSimpleName());
        }
    }

    public static <F, T> INodeFuncObject<T> castObjectToObject(INodeFuncObject<F> nodeFrom, Class<T> clazz)
        throws InvalidExpressionException {
        Class<F> fromClass = nodeFrom.getType();
        if (fromClass == clazz) {
            return (INodeFuncObject<T>) nodeFrom;
        } else {
            NodeType2<T> typeTo = NodeTypes.getType(clazz);
            Function<F, T> caster = typeTo.getCast(fromClass);
            if (caster == null) {
                throw new InvalidExpressionException("Cannot cast " + nodeFrom + " to " + clazz.getSimpleName());
            }
            return new NodeFuncObjectToObject<>(fromClass, clazz, caster);
        }
    }
}
