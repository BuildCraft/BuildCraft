package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeBinaryLongToBoolean implements INodeBoolean {
    @FunctionalInterface
    public interface BiLongToBooleanFunction {
        boolean apply(long l, long r);
    }

    private final INodeLong left, right;
    private final BiLongToBooleanFunction func;
    private final String op;

    public NodeBinaryLongToBoolean(INodeLong left, INodeLong right, BiLongToBooleanFunction func, String op) {
        this.left = left;
        this.right = right;
        this.func = func;
        this.op = op;
    }

    @Override
    public boolean evaluate() {
        return func.apply(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeBoolean inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryLongToBoolean(l, r, func, op), //
            (l, r) -> NodeConstantBoolean.get(func.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + op + " (" + right + ")";
    }
}
