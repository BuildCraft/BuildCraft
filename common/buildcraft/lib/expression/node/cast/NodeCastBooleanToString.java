package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.simple.NodeValueBoolean;
import buildcraft.lib.expression.node.simple.NodeValueString;

public class NodeCastBooleanToString implements INodeString {
    private final INodeBoolean from;

    public NodeCastBooleanToString(INodeBoolean from) {
        this.from = from;
    }

    @Override
    public String evaluate() {
        return Boolean.toString(from.evaluate());
    }

    @Override
    public INodeString inline(Arguments args) {
        INodeBoolean from = this.from.inline(args);
        if (from instanceof NodeValueBoolean) {
            return new NodeValueString(Boolean.toString(((NodeValueBoolean) from).value));
        } else if (from == this.from) {
            return this;
        } else {
            return new NodeCastBooleanToString(from);
        }
    }
}
