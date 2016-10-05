package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public class NodeBinaryDoubleToBoolean implements INodeBoolean {
    public enum Type {
        EQUAL((l, r) -> l == r),
        NOT_EQUAL((l, r) -> l != r),
        LESS_THAN((l, r) -> l < r),
        LESS_THAN_OR_EQUAL((l, r) -> l <= r),
        GREATER_THAN((l, r) -> l > r),
        GREATER_THAN_OR_EQUAL((l, r) -> l >= r);

        private final BiDoubleToBooleanFunction operator;

        private Type(BiDoubleToBooleanFunction operator) {
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
    public INodeBoolean inline(Arguments args) {
        INodeDouble il = this.left.inline(args);
        INodeDouble ir = this.right.inline(args);

        if (il instanceof NodeValueDouble && ir instanceof NodeValueDouble) {
            double l = ((NodeValueDouble) il).value;
            double r = ((NodeValueDouble) ir).value;
            return NodeValueBoolean.get(type.operator.apply(l, r));
        } else if (il == this.left && ir == this.right) {
            return this;
        } else {
            return new NodeBinaryDoubleToBoolean(il, ir, type);
        }
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.name() + " (" + right + ")";
    }

}
