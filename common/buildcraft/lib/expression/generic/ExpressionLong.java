package buildcraft.lib.expression.generic;

import buildcraft.lib.expression.generic.Arguments.ArgumentCounts;
import buildcraft.lib.expression.generic.IExpressionNode.INodeLong;

public class ExpressionLong implements INodeLong, IExpression {
    private final INodeLong node;
    private final ArgumentCounts counts;

    public ExpressionLong(INodeLong node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    public long evaluate() {
        return evaluate(Arguments.NO_ARGS);
    }

    @Override
    public long evaluate(Arguments args) {
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
