package buildcraft.lib.expression.api;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.*;

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

    public IVariableNode makeVariableNode() {
        switch (this) {
            case BOOLEAN:
                return new NodeVariableBoolean();
            case DOUBLE:
                return new NodeVariableDouble();
            case LONG:
                return new NodeVariableLong();
            case STRING:
                return new NodeVariableString();
            default:
                throw new IllegalStateException("Unknown node type '" + this + "'");
        }
    }
}
