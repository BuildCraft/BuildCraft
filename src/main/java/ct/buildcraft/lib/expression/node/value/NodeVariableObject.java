/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.value;

import ct.buildcraft.lib.expression.api.IDependancyVisitor;
import ct.buildcraft.lib.expression.api.IDependantNode;
import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IVariableNode.IVariableNodeObject;
import ct.buildcraft.lib.expression.api.NodeType;
import ct.buildcraft.lib.expression.api.NodeTypes;

public class NodeVariableObject<T> extends NodeVariable implements IVariableNodeObject<T>, IDependantNode {
    public final Class<T> type;
    public T value;
    private INodeObject<T> src;

    public NodeVariableObject(String name, Class<T> type) {
        super(name);
        this.type = type;
        NodeType<T> nodeType = NodeTypes.getType(type);
        if (nodeType == null) {
            throw new IllegalArgumentException("Unknown NodeType " + type);
        }
        this.value = nodeType.defaultValue;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T evaluate() {
        return src != null ? src.evaluate() : value;
    }

    @Override
    public INodeObject<T> inline() {
        if (isConst) {
            return new NodeConstantObject<>(getType(), value);
        } else if (src != null) {
            return src.inline();
        }
        return this;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public void setConstantSource(IExpressionNode source) {
        if (src != null) {
            throw new IllegalStateException("Already have a constant source");
        }
        INodeObject<?> obj = (INodeObject<?>) source;
        if (obj.getType() != getType()) {
            throw new IllegalArgumentException("Cannot convert " + obj.getType() + " to " + getType());
        }
        src = (INodeObject<T>) source;
    }

    @Override
    public void visitDependants(IDependancyVisitor visitor) {
        if (src != null) {
            visitor.dependOn(src);
        } else {
            visitor.dependOnExplictly(this);
        }
    }
}
