package buildcraft.lib.expression.api;

public interface IExpressionNode {
    default IExpressionNode inline() {
        return this;
    }

    // common expression types

    @FunctionalInterface
    public interface INodeDouble extends IExpressionNode {
        double evaluate();

        @Override
        default INodeDouble inline() {
            return this;
        }
    }

    @FunctionalInterface
    public interface INodeLong extends IExpressionNode {
        long evaluate();

        @Override
        default INodeLong inline() {
            return this;
        }
    }

    @FunctionalInterface
    public interface INodeBoolean extends IExpressionNode {
        boolean evaluate();

        @Override
        default INodeBoolean inline() {
            return this;
        }
    }

    @FunctionalInterface
    public interface INodeString extends IExpressionNode {
        String evaluate();

        @Override
        default INodeString inline() {
            return this;
        }
    }
}
