package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public interface IBinaryNodeType {
    IExpressionNode createLongNode(INodeLong l, INodeLong r) throws InvalidExpressionException;

    IExpressionNode createDoubleNode(INodeDouble l, INodeDouble r) throws InvalidExpressionException;

    IExpressionNode createBooleanNode(INodeBoolean l, INodeBoolean r) throws InvalidExpressionException;

    IExpressionNode createStringNode(INodeString l, INodeString r) throws InvalidExpressionException;

    default IExpressionNode createNode(IExpressionNode left, IExpressionNode right) throws InvalidExpressionException {
        left = InternalCompiler.convertBinary(left, right);
        right = InternalCompiler.convertBinary(right, left);

        if (left instanceof INodeLong) {
            return createLongNode((INodeLong) left, (INodeLong) right);
        } else if (left instanceof INodeDouble) {
            return createDoubleNode((INodeDouble) left, (INodeDouble) right);
        } else if (left instanceof INodeBoolean) {
            return createBooleanNode((INodeBoolean) left, (INodeBoolean) right);
        } else if (left instanceof INodeString) {
            return createStringNode((INodeString) left, (INodeString) right);
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }
}
