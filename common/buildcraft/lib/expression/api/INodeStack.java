/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public interface INodeStack {
    INodeLong popLong() throws InvalidExpressionException;

    INodeDouble popDouble() throws InvalidExpressionException;

    INodeBoolean popBoolean() throws InvalidExpressionException;

    INodeString popString() throws InvalidExpressionException;

    default IExpressionNode pop(NodeType type) throws InvalidExpressionException {
        if (type == NodeType.LONG) return popLong();
        else if (type == NodeType.DOUBLE) return popDouble();
        else if (type == NodeType.BOOLEAN) return popBoolean();
        else if (type == NodeType.STRING) return popString();
        else throw new IllegalArgumentException("Unknown node type " + type);
    }
}
