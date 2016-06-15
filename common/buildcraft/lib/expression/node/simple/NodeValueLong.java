package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeValueLong implements INodeLong {
    public final long value;

    public NodeValueLong(long value) {
        this.value = value;
    }

    @Override
    public long evaluate() {
        return value;
    }

    @Override
    public INodeLong inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return value + "L";
    }
}
