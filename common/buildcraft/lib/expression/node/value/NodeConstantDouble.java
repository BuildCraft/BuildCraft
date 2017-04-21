package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeConstantDouble implements INodeDouble, IConstantNode {
    public static final NodeConstantDouble ZERO = new NodeConstantDouble(0);
    public static final NodeConstantDouble ONE = new NodeConstantDouble(1);

    public final double value;

    public NodeConstantDouble(double value) {
        this.value = value;
    }

    @Override
    public double evaluate() {
        return value;
    }

    @Override
    public INodeDouble inline() {
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(value) + "D";
    }
}
