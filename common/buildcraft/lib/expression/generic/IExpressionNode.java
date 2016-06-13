package buildcraft.lib.expression.generic;

public interface IExpressionNode {
    @FunctionalInterface
    public interface INodeDouble extends IExpressionNode {
        double evaluate(Arguments args);
    }

    @FunctionalInterface
    public interface INodeLong extends IExpressionNode {
        long evaluate(Arguments args);
    }

    @FunctionalInterface
    public interface INodeBoolean extends IExpressionNode {
        boolean evaluate(Arguments args);
    }

    @FunctionalInterface
    public interface INodeString extends IExpressionNode {
        String evaluate(Arguments args);
    }
}
