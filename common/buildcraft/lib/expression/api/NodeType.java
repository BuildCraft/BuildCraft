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
import buildcraft.lib.expression.node.cast.NodeCasting;
import buildcraft.lib.expression.node.value.*;

public enum NodeType {
    LONG,
    DOUBLE,
    BOOLEAN,
    STRING;

    public static NodeType parseType(String type) throws InvalidExpressionException {
        for (NodeType n : values()) {
            if (n.name().equalsIgnoreCase(type)) {
                return n;
            }
        }
        throw new InvalidExpressionException("Unknown type " + type + ", must be one of ['long', 'double', 'boolean', 'string'], without the quotes.");
    }

    public static NodeType getType(IExpressionNode node) {
        if (node instanceof INodeLong) return LONG;
        else if (node instanceof INodeDouble) return DOUBLE;
        else if (node instanceof INodeBoolean) return BOOLEAN;
        else if (node instanceof INodeString) return STRING;
        else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }

    public IVariableNode makeVariableNode(String name) {
        switch (this) {
            case BOOLEAN:
                return new NodeVariableBoolean(name);
            case DOUBLE:
                return new NodeVariableDouble(name);
            case LONG:
                return new NodeVariableLong(name);
            case STRING:
                return new NodeVariableString(name);
            default:
                throw new IllegalStateException("Unknown node type '" + this + "'");
        }
    }

    public static IConstantNode createConstantNode(IExpressionNode node) {
        if (node instanceof INodeLong) return new NodeConstantLong(((INodeLong) node).evaluate());
        else if (node instanceof INodeDouble) return new NodeConstantDouble(((INodeDouble) node).evaluate());
        else if (node instanceof INodeBoolean) return NodeConstantBoolean.get(((INodeBoolean) node).evaluate());
        else if (node instanceof INodeString) return new NodeConstantString(((INodeString) node).evaluate());
        else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }

    public IExpressionNode cast(IExpressionNode node) throws InvalidExpressionException {
        switch (this) {
            case DOUBLE:
                return NodeCasting.castToDouble(node);
            case STRING:
                return NodeCasting.castToString(node);
            case LONG: {
                if (node instanceof INodeLong) {
                    return node;
                } else {
                    throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a long");
                }
            }
            case BOOLEAN: {
                if (node instanceof INodeBoolean) {
                    return node;
                } else {
                    throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a boolean");
                }
            }
            default:
                throw new IllegalStateException("Unknown node type '" + this + "'");
        }
    }
}
