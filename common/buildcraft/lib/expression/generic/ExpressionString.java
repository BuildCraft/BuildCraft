package buildcraft.lib.expression.generic;

import buildcraft.lib.expression.generic.Arguments.ArgumentCounts;
import buildcraft.lib.expression.generic.IExpressionNode.INodeString;

public class ExpressionString implements INodeString, IExpression {
    private final INodeString node;
    private final ArgumentCounts counts;

    public ExpressionString(INodeString node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    public String evaluate() {
        return evaluate(Arguments.NO_ARGS);
    }

    @Override
    public String evaluate(Arguments args) {
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
