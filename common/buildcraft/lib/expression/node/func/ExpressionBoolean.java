package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.ArgumentCounts;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpression.IExpressionBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

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
        GenericExpressionCompiler.debugStart("Deriving from " + args);
        INodeBoolean n = node.inline(args);
        GenericExpressionCompiler.debugEnd("Derived as " + n);
        return n;
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
