package buildcraft.lib.expression.node.arg;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeArgumentDouble implements INodeDouble {
    private final int index;

    public NodeArgumentDouble(int index) {
        this.index = index;
    }

    @Override
    public double evaluate() {
        throw new IllegalStateException("Cannot evaluate without optimizing!");
    }

    @Override
    public INodeDouble inline(Arguments args) {
        if (args == null) {
            return this;
        }
        return args.doubles[index];
    }

    @Override
    public String toString() {
        return "Argument(Double)#" + index;
    }
}
