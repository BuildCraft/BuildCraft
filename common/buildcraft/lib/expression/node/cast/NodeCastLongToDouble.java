package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.simple.NodeValueDouble;
import buildcraft.lib.expression.node.simple.NodeValueLong;

public class NodeCastLongToDouble implements INodeDouble {
    private final INodeLong from;

    public NodeCastLongToDouble(INodeLong from) {
        this.from = from;
    }

    @Override
    public double evaluate() {
        return from.evaluate();
    }

    @Override
    public INodeDouble inline(Arguments args) {
        INodeLong from = this.from.inline(args);
        if (from instanceof NodeValueLong) {
            return new NodeValueDouble(((NodeValueLong) from).value);
        } else if (from == this.from) {
            return this;
        } else {
            return new NodeCastLongToDouble(from);
        }
    }

    @Override
    public String toString() {
        return "( " + from + " ) -> double";
    }
}
