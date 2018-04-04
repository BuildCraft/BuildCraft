/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;

import java.util.ArrayList;
import java.util.List;

/** A node stack that returns immutable values containing either 0, false or the empty string. Used to count the number
 * of nodes that a particular function requires. */
public class NodeStackRecording implements INodeStack {

    public final List<Class<?>> types = new ArrayList<>();

    public Class<?>[] toArray() {
        return types.toArray(new Class[0]);
    }

    @Override
    public INodeLong popLong() throws InvalidExpressionException {
        types.add(long.class);
        return NodeConstantLong.ZERO;
    }

    @Override
    public INodeDouble popDouble() throws InvalidExpressionException {
        types.add(double.class);
        return NodeConstantDouble.ZERO;
    }

    @Override
    public INodeBoolean popBoolean() throws InvalidExpressionException {
        types.add(boolean.class);
        return NodeConstantBoolean.FALSE;
    }

    @Override
    public <T> INodeObject<T> popObject(Class<T> type) throws InvalidExpressionException {
        types.add(type);
        NodeType<T> nodeType = NodeTypes.getType(type);
        if (nodeType == null) {
            throw new IllegalStateException("Unknown " + type);
        }
        return new NodeConstantObject<>(type, nodeType.defaultValue);
    }
}
