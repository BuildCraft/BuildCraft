/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantString;

/** A node stack that returns immutable values containing either 0, false or the empty string. Used to count the number
 * of nodes that a particular function requires. */
public class NodeStackRecording implements INodeStack {

    public final List<NodeType> types = new ArrayList<>();

    public NodeType[] toArray() {
        return types.toArray(new NodeType[types.size()]);
    }

    @Override
    public INodeLong popLong() throws InvalidExpressionException {
        types.add(NodeType.LONG);
        return NodeConstantLong.ZERO;
    }

    @Override
    public INodeDouble popDouble() throws InvalidExpressionException {
        types.add(NodeType.DOUBLE);
        return NodeConstantDouble.ZERO;
    }

    @Override
    public INodeBoolean popBoolean() throws InvalidExpressionException {
        types.add(NodeType.BOOLEAN);
        return NodeConstantBoolean.FALSE;
    }

    @Override
    public INodeString popString() throws InvalidExpressionException {
        types.add(NodeType.STRING);
        return NodeConstantString.EMPTY;
    }
}
