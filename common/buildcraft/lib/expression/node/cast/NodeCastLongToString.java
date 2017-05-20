package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantString;

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
    public INodeString inline() {
        return NodeInliningHelper.tryInline(this, from, NodeCastLongToString::new, (f) -> new NodeConstantString(Long.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
