package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeImmutableBoolean;

public class NodeBinaryBoolean implements INodeBoolean {
    public enum Type {
        EQUAL("==", (l, r) -> l == r),
        NOT_EQUAL("!=", (l, r) -> l != r),
        AND("&&", (l, r) -> l & r),
        OR("||", (l, r) -> l | r);

        private final String op;
        private final BiBooleanPredicate operator;

        private Type(String op, BiBooleanPredicate operator) {
            this.op = op;
            this.operator = operator;
        }

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
        return NodeInliningHelper.tryInline(this, args, left, right, //
                (l, r) -> new NodeBinaryBoolean(l, r, type), //
                (l, r) -> NodeImmutableBoolean.get(type.operator.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + type.op + " (" + right + ")";
    }
}
