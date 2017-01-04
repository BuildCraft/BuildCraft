package buildcraft.lib.expression.api;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public enum NodeType {
    LONG,
    DOUBLE,
    BOOLEAN,
    STRING;

    public static NodeType getType(IExpressionNode node) {
        if (node instanceof INodeLong) return LONG;
        else if (node instanceof INodeDouble) return DOUBLE;
        else if (node instanceof INodeBoolean) return BOOLEAN;
        else if (node instanceof INodeString) return STRING;
        else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }
}
