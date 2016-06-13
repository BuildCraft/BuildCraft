package buildcraft.lib.expression.generic;

import buildcraft.lib.expression.generic.Arguments.ArgumentCounts;
import buildcraft.lib.expression.generic.IExpressionNode.INodeDouble;

public class ExpressionDouble implements INodeDouble, IExpression {
    private final INodeDouble node;
    private final ArgumentCounts counts;

    public ExpressionDouble(INodeDouble node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    public double evaluate() {
        return evaluate(Arguments.NO_ARGS);
    }

    @Override
    public double evaluate(Arguments args) {
        if (!counts.matches(args)) {
            throw new IllegalArgumentException("Illegal Arguments " + args + " when we needed " + counts);
        }
        return node.evaluate(args);
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }
}
