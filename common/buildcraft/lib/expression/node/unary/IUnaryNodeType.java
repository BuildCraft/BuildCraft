/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.unary;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.InvalidExpressionException;

public interface IUnaryNodeType {
    IExpressionNode createLongNode(INodeLong n) throws InvalidExpressionException;

    IExpressionNode createDoubleNode(INodeDouble n) throws InvalidExpressionException;

    IExpressionNode createBooleanNode(INodeBoolean n) throws InvalidExpressionException;

    IExpressionNode createStringNode(INodeObject<String> n) throws InvalidExpressionException;
}
