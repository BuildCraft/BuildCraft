package buildcraft.lib.expression.generic.node;

import buildcraft.lib.expression.generic.Arguments;
import buildcraft.lib.expression.generic.ExpressionBoolean;
import buildcraft.lib.expression.generic.IExpressionNode.INodeBoolean;

public class NodeFunctionBoolean implements INodeBoolean {
    private final ExpressionBoolean expression;
    private final EvaluatableArguments args;

    public NodeFunctionBoolean(ExpressionBoolean expression, EvaluatableArguments args) {
        this.expression = expression;
        this.args = args;
    }

    @Override
    public boolean evaluate(Arguments args) {
        return expression.evaluate(this.args.evaluate(args));
    }
}
