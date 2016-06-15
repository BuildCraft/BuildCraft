package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.ArgumentCounts;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpression.IExpressionBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public class ExpressionBoolean implements IExpressionBoolean {
    private final INodeBoolean node;
    private final ArgumentCounts counts;

    public ExpressionBoolean(INodeBoolean node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }

    @Override
    public INodeBoolean derive(Arguments args) {
        return node.inline(args);
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
