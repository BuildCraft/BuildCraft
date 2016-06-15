package buildcraft.lib.expression.node.simple;

import java.util.function.DoubleBinaryOperator;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeBinaryDouble implements INodeDouble {
    public enum Type {
        ADD((l, r) -> l + r),
        SUB((l, r) -> l - r),
        MUL((l, r) -> l * r),
        DIV((l, r) -> l / r),
        MOD((l, r) -> l % r),
        POW((l, r) -> Math.pow(l, r)),
        // Space for more :)
        ;

        private Type(DoubleBinaryOperator operator) {
            this.operator = operator;
        }

        private final DoubleBinaryOperator operator;

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
        INodeDouble left = this.left.inline(args);
        INodeDouble right = this.right.inline(args);

        if (left instanceof NodeValueDouble && right instanceof NodeValueDouble) {
            double l = ((NodeValueDouble) left).value;
            double r = ((NodeValueDouble) right).value;
            return new NodeValueDouble(type.operator.applyAsDouble(l, r));
        } else if (left == this.left && right == this.right) {
            return this;
        } else {
            return new NodeBinaryDouble(left, right, type);
        }
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.name() + " (" + right + ")";
    }
}
