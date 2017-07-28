/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeObject;
import buildcraft.lib.expression.api.NodeType2;
import buildcraft.lib.expression.api.NodeTypes;

public class NodeVariableObject<T> implements IVariableNodeObject<T> {
    public final String name;
    public final Class<T> type;
    public T value;
    private boolean isConst = false;

    public NodeVariableObject(String name, Class<T> type) {
        this.name = name;
        this.type = type;
        NodeType2<T> nodeType = NodeTypes.getType(type);
        if (nodeType == null) {
            throw new IllegalArgumentException("Unknown NodeType " + type);
        }
        this.value = nodeType.defaultValue;
    }

    @Override
    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T evaluate() {
        return value;
    }

    @Override
    public INodeObject<T> inline() {
        if (isConst) {
            return new NodeConstantObject<>(getType(), value);
        }
        return this;
    }

    @Override
    public void set(IExpressionNode from) {
        INodeObject<?> nodeFrom = (INodeObject<?>) from;
        if (nodeFrom.getType() != getType()) {
            throw new IllegalArgumentException("Wrong type! Expected " + getType() + " but got " + nodeFrom.getType());
        }
        value = (T) nodeFrom.evaluate();
    }

    @Override
    public String toString() {
        return name + " = " + evaluateAsString();
    }

    @Override
    public String getName() {
        return name;
    }
}
