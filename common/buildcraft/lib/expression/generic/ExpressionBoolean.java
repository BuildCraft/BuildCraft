package buildcraft.lib.expression.generic;

import buildcraft.lib.expression.generic.Arguments.ArgumentCounts;
import buildcraft.lib.expression.generic.IExpressionNode.INodeBoolean;

public class ExpressionBoolean implements INodeBoolean, IExpression {
    private final INodeBoolean node;
    private final ArgumentCounts counts;

    public ExpressionBoolean(INodeBoolean node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    public boolean evaluate() {
        return evaluate(Arguments.NO_ARGS);
    }

    @Override
    public boolean evaluate(Arguments args) {
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
