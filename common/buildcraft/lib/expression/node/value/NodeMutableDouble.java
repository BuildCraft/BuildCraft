package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeMutableDouble implements INodeDouble {
    public double value;

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
        return "mutable_double#" + System.identityHashCode(this);
    }
}
