package buildcraft.lib.expression.node.unary;

import java.util.function.DoubleUnaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeUnaryDouble implements INodeDouble {
    private final INodeDouble from;
    private final DoubleUnaryOperator func;
    private final String op;

    public NodeUnaryDouble(INodeDouble from, DoubleUnaryOperator func, String op) {
        this.from = from;
        this.func = func;
        this.op = op;
    }

    @Override
    public double evaluate() {
        return func.applyAsDouble(from.evaluate());
    }

    @Override
    public INodeDouble inline() {
        return NodeInliningHelper.tryInline(this, from, (f) -> new NodeUnaryDouble(f, func, op), //
            (f) -> new NodeConstantDouble(func.applyAsDouble(f.evaluate())));
    }

    @Override
    public String toString() {
        return op + "(" + from + ")";
    }
}
