package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.ArgumentCounts;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpression.IExpressionDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class ExpressionDouble implements IExpressionDouble {
    private final INodeDouble node;
    private final ArgumentCounts counts;

    public ExpressionDouble(INodeDouble node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }

    @Override
    public INodeDouble derive(Arguments args) {
        return node.inline(args);
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
