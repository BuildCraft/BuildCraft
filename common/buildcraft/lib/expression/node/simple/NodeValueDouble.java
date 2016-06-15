package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeValueDouble implements INodeDouble {
    public final double value;

    public NodeValueDouble(double value) {
        this.value = value;
    }

    @Override
    public double evaluate() {
        return value;
    }

    @Override
    public INodeDouble inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(value) + "D";
    }
}
