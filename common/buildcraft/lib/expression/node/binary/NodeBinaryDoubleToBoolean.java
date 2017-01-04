package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeBinaryDoubleToBoolean implements INodeBoolean {
    public enum Type {
        EQUAL("==", (l, r) -> l == r),
        NOT_EQUAL("!=", (l, r) -> l != r),
        LESS_THAN("<", (l, r) -> l < r),
        LESS_THAN_OR_EQUAL("<=", (l, r) -> l <= r),
        GREATER_THAN(">", (l, r) -> l > r),
        GREATER_THAN_OR_EQUAL(">=", (l, r) -> l >= r);

        private final String op;
        private final BiDoubleToBooleanFunction operator;

        Type(String op, BiDoubleToBooleanFunction operator) {
            this.op = op;
            this.operator = operator;
        }

        public NodeBinaryDoubleToBoolean create(INodeDouble left, INodeDouble right) {
            return new NodeBinaryDoubleToBoolean(left, right, this);
        }
    }

    @FunctionalInterface
    public interface BiDoubleToBooleanFunction {
        boolean apply(double l, double r);
    }

    private final INodeDouble left, right;
    private final Type type;

    private NodeBinaryDoubleToBoolean(INodeDouble left, INodeDouble right, Type type) {
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
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryDoubleToBoolean(l, r, type), //
                (l, r) -> NodeConstantBoolean.get(type.operator.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.op + " (" + right + ")";
    }

}
