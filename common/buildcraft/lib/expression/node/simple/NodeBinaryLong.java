package buildcraft.lib.expression.node.simple;

import java.util.function.LongBinaryOperator;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeBinaryLong implements INodeLong {
    public enum Type {
        ADD((l, r) -> l + r),
        SUB((l, r) -> l - r),
        MUL((l, r) -> l * r),
        DIV((l, r) -> l / r),
        MOD((l, r) -> l % r),
        POW((l, r) -> (long) Math.pow(l, r)),
        SHIFT_LEFT((l, r) -> l << r),
        SHIFT_RIGHT((l, r) -> l >> r),
        // Space for more :)
        ;

        private Type(LongBinaryOperator operator) {
            this.operator = operator;
        }

        private final LongBinaryOperator operator;

        public NodeBinaryLong create(INodeLong left, INodeLong right) {
            return new NodeBinaryLong(left, right, this);
        }
    }

    private final INodeLong left, right;
    private final Type type;

    private NodeBinaryLong(INodeLong left, INodeLong right, Type type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    @Override
    public long evaluate() {
        return type.operator.applyAsLong(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeLong inline(Arguments args) {
        INodeLong left = this.left.inline(args);
        INodeLong right = this.right.inline(args);

        if (left instanceof NodeValueLong && right instanceof NodeValueLong) {
            long l = ((NodeValueLong) left).value;
            long r = ((NodeValueLong) right).value;
            return new NodeValueLong(type.operator.applyAsLong(l, r));
        } else if (left == this.left && right == this.right) {
            return this;
        } else {
            return new NodeBinaryLong(left, right, type);
        }
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.name() + " (" + right + ")";
    }
}
