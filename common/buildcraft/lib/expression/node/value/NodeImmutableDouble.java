package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeImmutableDouble implements INodeDouble, IImmutableNode {
    public final double value;

    public NodeImmutableDouble(double value) {
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
