package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.ArgumentCounts;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpression.IExpressionString;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public class ExpressionString implements IExpressionString {
    private final INodeString node;
    private final ArgumentCounts counts;

    public ExpressionString(INodeString node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }

    @Override
    public INodeString derive(Arguments args) {
        return node.inline(args);
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
