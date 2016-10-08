package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeMutableLong implements INodeLong {
    public long value;

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
        return "mutable_long#" + System.identityHashCode(this);
    }
}
