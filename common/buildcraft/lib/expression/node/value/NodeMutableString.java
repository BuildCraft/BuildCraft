package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public class NodeMutableString implements INodeString {
    public String value = "";

    @Override
    public String evaluate() {
        return value;
    }

    @Override
    public INodeString inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return "mutable_string#" + System.identityHashCode(this);
    }
}
