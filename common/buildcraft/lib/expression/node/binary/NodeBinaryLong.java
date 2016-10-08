package buildcraft.lib.expression.node.binary;

import java.util.function.LongBinaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeImmutableLong;

public class NodeBinaryLong implements INodeLong {
    public enum Type {
        ADD("+", (l, r) -> l + r),
        SUB("-", (l, r) -> l - r),
        MUL("*", (l, r) -> l * r),
        DIV("/", (l, r) -> l / r),
        MOD("%", (l, r) -> l % r),
        POW("^", (l, r) -> (long) Math.pow(l, r)),
        SHIFT_LEFT("<<", (l, r) -> l << r),
        SHIFT_RIGHT(">>", (l, r) -> l >> r);

        private final String op;
        private final LongBinaryOperator operator;

        Type(String op, LongBinaryOperator operator) {
            this.op = op;
            this.operator = operator;
        }

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
        return NodeInliningHelper.tryInline(this, args, left, right, //
                (l, r) -> new NodeBinaryLong(l, r, type), //
                (l, r) -> new NodeImmutableLong(type.operator.applyAsLong(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.op + " (" + right + ")";
    }
}
