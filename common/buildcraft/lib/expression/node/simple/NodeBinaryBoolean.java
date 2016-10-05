package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public class NodeBinaryBoolean implements INodeBoolean {
    public enum Type {
        EQUAL((l, r) -> l == r),
        NOT_EQUAL((l, r) -> l != r),
        AND((l, r) -> l & r),
        OR((l, r) -> l | r);

        private Type(BiBooleanPredicate operator) {
            this.operator = operator;
        }

        private final BiBooleanPredicate operator;

        public NodeBinaryBoolean create(INodeBoolean left, INodeBoolean right) {
            return new NodeBinaryBoolean(left, right, this);
        }
    }

    @FunctionalInterface
    public interface BiBooleanPredicate {
        boolean apply(boolean left, boolean right);
    }

    private final INodeBoolean left, right;
    private final Type type;

    private NodeBinaryBoolean(INodeBoolean left, INodeBoolean right, Type type) {
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
        INodeBoolean il = this.left.inline(args);
        INodeBoolean ir = this.right.inline(args);

        if (il instanceof NodeValueBoolean && ir instanceof NodeValueBoolean) {
            boolean l = ((NodeValueBoolean) il).value;
            boolean r = ((NodeValueBoolean) ir).value;
            return NodeValueBoolean.get(type.operator.apply(l, r));
        } else if (il == this.left && ir == this.right) {
            return this;
        } else {
            return new NodeBinaryBoolean(il, ir, type);
        }
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.name() + " (" + right + ")";
    }
}
