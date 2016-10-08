package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public class NodeMutableBoolean implements INodeBoolean {
    public boolean value;

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return "mutable_boolean#" + System.identityHashCode(this);
    }
}
