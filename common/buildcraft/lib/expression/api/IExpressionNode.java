package buildcraft.lib.expression.api;

public interface IExpressionNode {
    IExpressionNode inline();

    // common expression types

    interface INodeDouble extends IExpressionNode {
        double evaluate();

        @Override
        INodeDouble inline();
    }

    interface INodeLong extends IExpressionNode {
        long evaluate();

        @Override
        INodeLong inline();
    }

    interface INodeBoolean extends IExpressionNode {
        boolean evaluate();

        @Override
        INodeBoolean inline();
    }

    interface INodeString extends IExpressionNode {
        String evaluate();

        @Override
        INodeString inline();
    }
}
