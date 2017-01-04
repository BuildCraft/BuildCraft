package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantString;

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
    public INodeString inline() {
        return NodeInliningHelper.tryInline(this, from, (f) -> new NodeCastDoubleToString(f), (f) -> new NodeConstantString(Double.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
