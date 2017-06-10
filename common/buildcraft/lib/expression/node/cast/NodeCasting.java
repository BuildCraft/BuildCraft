/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncString;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeCasting {
    public static INodeString castToString(IExpressionNode node) {
        if (node instanceof INodeString) {
            return (INodeString) node;
        }

        if (node instanceof INodeBoolean) {
            return new NodeCastBooleanToString((INodeBoolean) node);
        }

        if (node instanceof INodeLong) {
            return new NodeCastLongToString((INodeLong) node);
        }

        if (node instanceof INodeDouble) {
            return new NodeCastDoubleToString((INodeDouble) node);
        }

        // We have no idea what class this is, but it *must* be wrong
        ExpressionDebugManager.debugNodeClass(node.getClass());
        throw new IllegalStateException("Unknown node type " + node.getClass());
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

    public static INodeFuncString castToString(INodeFunc func) {
        if (func instanceof INodeFuncString) {
            return (INodeFuncString) func;
        }

        if (func instanceof INodeFuncBoolean) {
            final INodeFuncBoolean funcBool = (INodeFuncBoolean) func;
            return (stack) -> new NodeCastBooleanToString(funcBool.getNode(stack));
        }

        if (func instanceof INodeFuncLong) {
            final INodeFuncLong funcLong = (INodeFuncLong) func;
            return (stack) -> new NodeCastLongToString(funcLong.getNode(stack));
        }

        if (func instanceof INodeFuncDouble) {
            final INodeFuncDouble funcDouble = (INodeFuncDouble) func;
            return (stack) -> new NodeCastDoubleToString(funcDouble.getNode(stack));
        }

        // We have no idea what class this is, but it *must* be wrong
        ExpressionDebugManager.debugNodeClass(func.getClass());
        throw new IllegalStateException("Unknown node type " + func.getClass());
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
}
