package buildcraft.lib.expression.api;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public interface IExpression {
    ArgumentCounts getCounts();

    IExpressionNode derive(Arguments args);

    public interface IExpressionLong extends IExpression {
        @Override
        INodeLong derive(Arguments args);
    }

    public interface IExpressionDouble extends IExpression {
        @Override
        INodeDouble derive(Arguments args);
    }

    public interface IExpressionBoolean extends IExpression {
        @Override
        INodeBoolean derive(Arguments args);
    }

    public interface IExpressionString extends IExpression {
        @Override
        INodeString derive(Arguments args);
    }
}
