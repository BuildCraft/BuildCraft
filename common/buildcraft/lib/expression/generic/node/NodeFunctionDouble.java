package buildcraft.lib.expression.generic.node;

import buildcraft.lib.expression.generic.Arguments;
import buildcraft.lib.expression.generic.ExpressionDouble;
import buildcraft.lib.expression.generic.IExpressionNode.INodeDouble;

public class NodeFunctionDouble implements INodeDouble {
    private final ExpressionDouble expression;
    private final EvaluatableArguments args;

    public NodeFunctionDouble(ExpressionDouble expression, EvaluatableArguments args) {
        this.expression = expression;
        this.args = args;
    }

    @Override
    public double evaluate(Arguments args) {
        return expression.evaluate(this.args.evaluate(args));
    }
}
