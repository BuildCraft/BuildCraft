package buildcraft.lib.expression.api;

public interface IExpressionNode {
    IExpressionNode inline();

    // common expression types

    public interface INodeDouble extends IExpressionNode {
        double evaluate();

        @Override
        INodeDouble inline();
    }

    public interface INodeLong extends IExpressionNode {
        long evaluate();

        @Override
        INodeLong inline();
    }

    public interface INodeBoolean extends IExpressionNode {
        boolean evaluate();

        @Override
        INodeBoolean inline();
    }

    public interface INodeString extends IExpressionNode {
        String evaluate();

        @Override
        INodeString inline();
    }
}
