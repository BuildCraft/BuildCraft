/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.InvalidExpressionException;

@Deprecated
public interface IBinaryNodeType {
    IExpressionNode createLongNode(INodeLong l, INodeLong r) throws InvalidExpressionException;

    IExpressionNode createDoubleNode(INodeDouble l, INodeDouble r) throws InvalidExpressionException;

    IExpressionNode createBooleanNode(INodeBoolean l, INodeBoolean r) throws InvalidExpressionException;

    IExpressionNode createStringNode(INodeObject<String> l, INodeObject<String> r) throws InvalidExpressionException;

    <T> IExpressionNode createObjectNode(INodeObject<T> l, INodeObject<T> r) throws InvalidExpressionException;

    default IExpressionNode createNode(IExpressionNode left, IExpressionNode right) throws InvalidExpressionException {
        left = InternalCompiler.convertBinary(left, right);
        right = InternalCompiler.convertBinary(right, left);

        if (left instanceof INodeLong) {
            return createLongNode((INodeLong) left, (INodeLong) right);
        } else if (left instanceof INodeDouble) {
            return createDoubleNode((INodeDouble) left, (INodeDouble) right);
        } else if (left instanceof INodeBoolean) {
            return createBooleanNode((INodeBoolean) left, (INodeBoolean) right);
        } else if (left instanceof INodeObject) {
            INodeObject l = (INodeObject) left;
            INodeObject r = (INodeObject) right;
            Class type = l.getType();
            if (type == String.class) {
                return createStringNode(l, r);
            } else {
                return createObjectNode(l, r);
            }
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }
}
