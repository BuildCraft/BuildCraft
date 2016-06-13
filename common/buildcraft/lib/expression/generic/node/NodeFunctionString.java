package buildcraft.lib.expression.generic.node;

import buildcraft.lib.expression.generic.Arguments;
import buildcraft.lib.expression.generic.ExpressionString;
import buildcraft.lib.expression.generic.IExpressionNode.INodeString;

public class NodeFunctionString implements INodeString {
    private final ExpressionString expression;
    private final EvaluatableArguments args;

    public NodeFunctionString(ExpressionString expression, EvaluatableArguments args) {
        this.expression = expression;
        this.args = args;
    }

    @Override
    public String evaluate(Arguments args) {
        return expression.evaluate(this.args.evaluate(args));
    }
}
