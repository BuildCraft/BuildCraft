package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeImmutableString;

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
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeCastBooleanToString(f), (f) -> new NodeImmutableString(Boolean.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
