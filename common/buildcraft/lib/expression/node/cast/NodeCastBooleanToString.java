package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantString;

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
    public INodeString inline() {
        return NodeInliningHelper.tryInline(this, from, NodeCastBooleanToString::new, (f) -> new NodeConstantString(Boolean.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
