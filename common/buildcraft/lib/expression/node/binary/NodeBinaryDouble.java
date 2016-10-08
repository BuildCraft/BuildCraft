package buildcraft.lib.expression.node.binary;

import java.util.function.DoubleBinaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeImmutableDouble;

public class NodeBinaryDouble implements INodeDouble {
    public enum Type {
        ADD("+", (l, r) -> l + r),
        SUB("-", (l, r) -> l - r),
        MUL("*", (l, r) -> l * r),
        DIV("/", (l, r) -> l / r),
        MOD("%", (l, r) -> l % r),
        POW("^", (l, r) -> Math.pow(l, r));

        private final String op;
        private final DoubleBinaryOperator operator;

        private Type(String op, DoubleBinaryOperator operator) {
            this.op = op;
            this.operator = operator;
        }

        public NodeBinaryDouble create(INodeDouble left, INodeDouble right) {
            return new NodeBinaryDouble(left, right, this);
        }
    }

    private final INodeDouble left, right;
    private final Type type;

    private NodeBinaryDouble(INodeDouble left, INodeDouble right, Type type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    @Override
    public double evaluate() {
        return type.operator.applyAsDouble(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeDouble inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, left, right, //
                (l, r) -> new NodeBinaryDouble(l, r, type), //
                (l, r) -> new NodeImmutableDouble(type.operator.applyAsDouble(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.op + " (" + right + ")";
    }
}
