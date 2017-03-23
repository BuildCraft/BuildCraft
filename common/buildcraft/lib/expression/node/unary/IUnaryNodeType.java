package buildcraft.lib.expression.node.unary;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public interface IUnaryNodeType {
    IExpressionNode createLongNode(INodeLong n) throws InvalidExpressionException;

    IExpressionNode createDoubleNode(INodeDouble n) throws InvalidExpressionException;

    IExpressionNode createBooleanNode(INodeBoolean n) throws InvalidExpressionException;

    IExpressionNode createStringNode(INodeString n) throws InvalidExpressionException;
}
