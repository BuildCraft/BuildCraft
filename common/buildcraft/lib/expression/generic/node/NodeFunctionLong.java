package buildcraft.lib.expression.generic.node;

import buildcraft.lib.expression.generic.Arguments;
import buildcraft.lib.expression.generic.ExpressionLong;
import buildcraft.lib.expression.generic.IExpressionNode.INodeLong;

public class NodeFunctionLong implements INodeLong {
    private final ExpressionLong expression;
    private final EvaluatableArguments args;

    public NodeFunctionLong(ExpressionLong expression, EvaluatableArguments args) {
        this.expression = expression;
        this.args = args;
    }

    @Override
    public long evaluate(Arguments args) {
        return expression.evaluate(this.args.evaluate(args));
    }
}
