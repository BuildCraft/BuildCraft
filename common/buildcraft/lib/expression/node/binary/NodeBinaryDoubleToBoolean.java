package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeBinaryDoubleToBoolean implements INodeBoolean {
    @FunctionalInterface
    public interface BiDoubleToBooleanFunction {
        boolean apply(double l, double r);
    }

    private final INodeDouble left, right;
    private final BiDoubleToBooleanFunction func;
    private final String op;

    public NodeBinaryDoubleToBoolean(INodeDouble left, INodeDouble right, BiDoubleToBooleanFunction func, String op) {
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
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryDoubleToBoolean(l, r, func, op), //
            (l, r) -> NodeConstantBoolean.get(func.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + op + " (" + right + ")";
    }

}
