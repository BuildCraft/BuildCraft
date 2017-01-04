package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeBinaryLongToBoolean implements INodeBoolean {
    public enum Type {
        EQUAL("==", (l, r) -> l == r),
        NOT_EQUAL("!=", (l, r) -> l != r),
        LESS_THAN("<", (l, r) -> l < r),
        LESS_THAN_OR_EQUAL("<=", (l, r) -> l <= r),
        GREATER_THAN(">", (l, r) -> l > r),
        GREATER_THAN_OR_EQUAL(">=", (l, r) -> l >= r);

        private final String op;
        private final BiLongToBooleanFunction operator;

        Type(String op, BiLongToBooleanFunction operator) {
            this.op = op;
            this.operator = operator;
        }

        public NodeBinaryLongToBoolean create(INodeLong left, INodeLong right) {
            return new NodeBinaryLongToBoolean(left, right, this);
        }
    }

    @FunctionalInterface
    public interface BiLongToBooleanFunction {
        boolean apply(long l, long r);
    }

    private final INodeLong left, right;
    private final Type type;

    private NodeBinaryLongToBoolean(INodeLong left, INodeLong right, Type type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    @Override
    public boolean evaluate() {
        return type.operator.apply(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeBoolean inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryLongToBoolean(l, r, type), //
                (l, r) -> NodeConstantBoolean.get(type.operator.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.op + " (" + right + ")";
    }

}
