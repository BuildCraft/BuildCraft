package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeImmutableString;

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
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeCastLongToString(f), (f) -> new NodeImmutableString(Long.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
