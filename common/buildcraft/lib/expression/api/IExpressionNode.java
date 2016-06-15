package buildcraft.lib.expression.api;

import javax.annotation.Nullable;

public interface IExpressionNode {
    IExpressionNode inline(@Nullable Arguments args);

    public interface INodeDouble extends IExpressionNode {
        double evaluate();

        @Override
        INodeDouble inline(@Nullable Arguments args);
    }

    public interface INodeLong extends IExpressionNode {
        long evaluate();

        @Override
        INodeLong inline(@Nullable Arguments args);
    }

    public interface INodeBoolean extends IExpressionNode {
        boolean evaluate();

        @Override
        INodeBoolean inline(@Nullable Arguments args);
    }

    public interface INodeString extends IExpressionNode {
        String evaluate();

        @Override
        INodeString inline(@Nullable Arguments args);
    }
}
