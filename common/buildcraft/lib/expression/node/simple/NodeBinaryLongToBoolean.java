package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public class NodeBinaryLongToBoolean implements INodeBoolean {
    public enum Type {
        EQUAL((l, r) -> l == r),
        NOT_EQUAL((l, r) -> l != r),
        LESS_THAN((l, r) -> l < r),
        LESS_THAN_OR_EQUAL((l, r) -> l <= r),
        GREATER_THAN((l, r) -> l > r),
        GREATER_THAN_OR_EQUAL((l, r) -> l >= r);

        private final BiLongToBooleanFunction operator;

        private Type(BiLongToBooleanFunction operator) {
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
    public INodeBoolean inline(Arguments args) {
        INodeLong il = this.left.inline(args);
        INodeLong ir = this.right.inline(args);

        if (il instanceof NodeValueLong && ir instanceof NodeValueLong) {
            long l = ((NodeValueLong) il).value;
            long r = ((NodeValueLong) ir).value;
            return NodeValueBoolean.get(type.operator.apply(l, r));
        } else if (il == this.left && ir == this.right) {
            return this;
        } else {
            return new NodeBinaryLongToBoolean(il, ir, type);
        }
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.name() + " (" + right + ")";
    }

}
