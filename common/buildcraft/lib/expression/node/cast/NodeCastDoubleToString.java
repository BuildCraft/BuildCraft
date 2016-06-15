package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.simple.NodeValueDouble;
import buildcraft.lib.expression.node.simple.NodeValueString;

public class NodeCastDoubleToString implements INodeString {
    private final INodeDouble from;

    public NodeCastDoubleToString(INodeDouble from) {
        this.from = from;
    }

    @Override
    public String evaluate() {
        return Double.toString(from.evaluate());
    }

    @Override
    public INodeString inline(Arguments args) {
        INodeDouble from = this.from.inline(args);
        if (from instanceof NodeValueDouble) {
            return new NodeValueString(Double.toString(((NodeValueDouble) from).value));
        } else if (from == this.from) {
            return this;
        } else {
            return new NodeCastDoubleToString(from);
        }
    }
}
