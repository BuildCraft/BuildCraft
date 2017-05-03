package buildcraft.lib.expression.node.unary;

import java.util.function.DoubleUnaryOperator;
import java.util.function.LongUnaryOperator;

import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public enum UnaryNodeType implements IUnaryNodeType {
    NEGATE("-", n -> -n, n -> -n),
    BITWISE_INVERT("~", n -> ~n, null);

    public final String op;
    public final LongUnaryOperator longFunc;
    public final DoubleUnaryOperator doubleFunc;

    private UnaryNodeType(String op, LongUnaryOperator longFunc, DoubleUnaryOperator doubleFunc) {
        this.op = op;
        this.longFunc = longFunc;
        this.doubleFunc = doubleFunc;
    }

    @Override
    public INodeLong createLongNode(INodeLong n) throws InvalidExpressionException {
        if (longFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on long nodes!");
        }
        return new NodeUnaryLong(n, longFunc, op);
    }

    @Override
    public INodeDouble createDoubleNode(INodeDouble n) throws InvalidExpressionException {
        if (doubleFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on double nodes!");
        }
        return new NodeUnaryDouble(n, doubleFunc, op);
    }

    @Override
    public INodeBoolean createBooleanNode(INodeBoolean n) throws InvalidExpressionException {
        if (this != NEGATE) {
            throw new InvalidExpressionException("Cannot perform " + this + " on boolean nodes!");
        }
        return new NodeBooleanInvert(n);
    }

    @Override
    public INodeString createStringNode(INodeString n) throws InvalidExpressionException {
        throw new InvalidExpressionException("Cannot perform " + this + " on string nodes!");
    }
}
