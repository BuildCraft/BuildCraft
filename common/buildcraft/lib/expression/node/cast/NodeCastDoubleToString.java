package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeImmutableString;

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
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeCastDoubleToString(f), (f) -> new NodeImmutableString(Double.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
