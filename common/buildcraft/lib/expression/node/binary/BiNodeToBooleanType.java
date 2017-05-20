package buildcraft.lib.expression.node.binary;

import com.google.common.base.Objects;

import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.binary.NodeBinaryBoolean.BiBooleanPredicate;
import buildcraft.lib.expression.node.binary.NodeBinaryDoubleToBoolean.BiDoubleToBooleanFunction;
import buildcraft.lib.expression.node.binary.NodeBinaryLongToBoolean.BiLongToBooleanFunction;
import buildcraft.lib.expression.node.binary.NodeBinaryStringToBoolean.BiStringToBooleanFunction;

public enum BiNodeToBooleanType implements IBinaryNodeType {
    EQUAL("==", (l, r) -> l == r, (l, r) -> l == r, (l, r) -> l == r, Objects::equal),
    NOT_EQUAL("!=", (l, r) -> l != r, (l, r) -> l != r, (l, r) -> l != r, (l, r) -> !Objects.equal(l, r)),
    LESS_THAN("<", (l, r) -> l < r, (l, r) -> l < r, null, (l, r) -> l.compareTo(r) < 0),
    GREATER_THAN(">", (l, r) -> l > r, (l, r) -> l > r, null, (l, r) -> l.compareTo(r) > 0),
    LESS_THAN_OR_EQUAL("<=", (l, r) -> l <= r, (l, r) -> l <= r, null, (l, r) -> l.compareTo(r) <= 0),
    GREATER_THAN_OR_EQUAL(">=", (l, r) -> l >= r, (l, r) -> l >= r, null, (l, r) -> l.compareTo(r) >= 0);

    public final String op;
    public final BiLongToBooleanFunction longFunc;
    public final BiDoubleToBooleanFunction doubleFunc;
    public final BiBooleanPredicate booleanFunction;
    public final BiStringToBooleanFunction stringFunc;

    BiNodeToBooleanType(String op, BiLongToBooleanFunction longFunc, BiDoubleToBooleanFunction doubleFunc, BiBooleanPredicate booleanFunction, BiStringToBooleanFunction stringFunc) {
        this.op = op;
        this.longFunc = longFunc;
        this.doubleFunc = doubleFunc;
        this.booleanFunction = booleanFunction;
        this.stringFunc = stringFunc;
    }

    @Override
    public INodeBoolean createLongNode(INodeLong l, INodeLong r) throws InvalidExpressionException {
        if (longFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on long nodes!");
        }
        return new NodeBinaryLongToBoolean(l, r, longFunc, op);
    }

    @Override
    public INodeBoolean createDoubleNode(INodeDouble l, INodeDouble r) throws InvalidExpressionException {
        if (doubleFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on double nodes!");
        }
        return new NodeBinaryDoubleToBoolean(l, r, doubleFunc, op);
    }

    @Override
    public INodeBoolean createBooleanNode(INodeBoolean l, INodeBoolean r) throws InvalidExpressionException {
        if (booleanFunction == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on boolean nodes!");
        }
        return new NodeBinaryBoolean(l, r, booleanFunction, op);
    }

    @Override
    public INodeBoolean createStringNode(INodeString l, INodeString r) throws InvalidExpressionException {
        if (stringFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on string nodes!");
        }
        return new NodeBinaryStringToBoolean(l, r, stringFunc, op);
    }
}
