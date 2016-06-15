package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.simple.NodeValueLong;
import buildcraft.lib.expression.node.simple.NodeValueString;

public class NodeCastLongToString implements INodeString {
    private final INodeLong from;

    public NodeCastLongToString(INodeLong from) {
        this.from = from;
    }

    @Override
    public String evaluate() {
        return Long.toString(from.evaluate());
    }

    @Override
    public INodeString inline(Arguments args) {
        INodeLong from = this.from.inline(args);
        if (from instanceof NodeValueLong) {
            return new NodeValueString(Long.toString(((NodeValueLong) from).value));
        } else if (from == this.from) {
            return this;
        } else {
            return new NodeCastLongToString(from);
        }
    }
}
