package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.GenericExpressionCompiler;
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
        GenericExpressionCompiler.debugStart("Deriving from " + args);
        INodeString n = node.inline(args);
        GenericExpressionCompiler.debugEnd("Derived as " + n);
        return n;
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
