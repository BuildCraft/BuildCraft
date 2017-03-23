package buildcraft.lib.expression.node.binary;

import java.util.function.LongBinaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeBinaryLong implements INodeLong {
    private final INodeLong left, right;
    private final LongBinaryOperator func;
    private final String op;

    public NodeBinaryLong(INodeLong left, INodeLong right, LongBinaryOperator func, String op) {
        this.left = left;
        this.right = right;
        this.func = func;
        this.op = op;
    }

    @Override
    public long evaluate() {
        return func.applyAsLong(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeLong inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryLong(l, r, func, op), //
            (l, r) -> new NodeConstantLong(func.applyAsLong(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + op + " (" + right + ")";
    }
}
