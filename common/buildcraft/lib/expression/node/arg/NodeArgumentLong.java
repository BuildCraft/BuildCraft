package buildcraft.lib.expression.node.arg;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeArgumentLong implements INodeLong {
    private final int index;

    public NodeArgumentLong(int index) {
        this.index = index;
    }

    @Override
    public long evaluate() {
        throw new IllegalStateException("Cannot evaluate without optimizing!");
    }

    @Override
    public INodeLong inline(Arguments args) {
        if (args == null) {
            return this;
        }
        return args.longs[index].inline(args);// TODO: Make sure this is right!
    }

    @Override
    public String toString() {
        return "Argument(Long)#" + index;
    }
}
