package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeImmutableLong implements INodeLong, IImmutableNode {
    public final long value;

    public NodeImmutableLong(long value) {
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
