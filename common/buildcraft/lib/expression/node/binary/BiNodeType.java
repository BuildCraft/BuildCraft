package buildcraft.lib.expression.node.binary;

import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.binary.NodeBinaryBoolean.BiBooleanPredicate;
import buildcraft.lib.expression.node.binary.NodeBinaryString.BiStringFunction;

public enum BiNodeType implements IBinaryNodeType {
    AND("&", (l, r) -> l & r, null, (l, r) -> l & r, (l, r) -> l + r),
    OR("|", (l, r) -> l | r, null, (l, r) -> l | r, null),
    XOR("^", (l, r) -> l ^ r, null, (l, r) -> l ^ r, null),
    ADD("+", (l, r) -> l + r, (l, r) -> l + r, null, (l, r) -> l + r),
    SUB("-", (l, r) -> l - r, (l, r) -> l - r, null, null),
    MUL("*", (l, r) -> l * r, (l, r) -> l * r, null, null),
    DIV("/", (l, r) -> l / r, (l, r) -> l / r, null, null),
    MOD("%", (l, r) -> l % r, (l, r) -> l % r, null, null),
    SHIFT_RIGHT("<<", (l, r) -> l << r, null, null, null),
    SHIFT_LEFT(">>", (l, r) -> l >> r, null, null, null);

    public final String op;
    public final LongBinaryOperator longFunc;
    public final DoubleBinaryOperator doubleFunc;
    public final BiBooleanPredicate booleanFunction;
    public final BiStringFunction stringFunc;

    private BiNodeType(String op, LongBinaryOperator longFunc, DoubleBinaryOperator doubleFunc, BiBooleanPredicate booleanFunction, BiStringFunction stringFunc) {
        this.op = op;
        this.longFunc = longFunc;
        this.doubleFunc = doubleFunc;
        this.booleanFunction = booleanFunction;
        this.stringFunc = stringFunc;
    }

    @Override
    public INodeLong createLongNode(INodeLong l, INodeLong r) throws InvalidExpressionException {
        if (longFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on long nodes!");
        }
        return new NodeBinaryLong(l, r, longFunc, op);
    }

    @Override
    public INodeDouble createDoubleNode(INodeDouble l, INodeDouble r) throws InvalidExpressionException {
        if (doubleFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on double nodes!");
        }
        return new NodeBinaryDouble(l, r, doubleFunc, op);
    }

    @Override
    public INodeBoolean createBooleanNode(INodeBoolean l, INodeBoolean r) throws InvalidExpressionException {
        if (booleanFunction == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on boolean nodes!");
        }
        return new NodeBinaryBoolean(l, r, booleanFunction, op);
    }

    @Override
    public INodeString createStringNode(INodeString l, INodeString r) throws InvalidExpressionException {
        if (stringFunc == null) {
            throw new InvalidExpressionException("Cannot perform " + this + " on string nodes!");
        }
        return new NodeBinaryString(l, r, stringFunc, op);
    }
}
