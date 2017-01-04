package buildcraft.lib.expression.node.unary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeBooleanInvert implements INodeBoolean {
    private final INodeBoolean from;

    public NodeBooleanInvert(INodeBoolean from) {
        this.from = from;
    }

    @Override
    public boolean evaluate() {
        return !from.evaluate();
    }

    @Override
    public INodeBoolean inline() {
        return NodeInliningHelper.tryInline(this, from, (f) -> new NodeBooleanInvert(f), (f) -> NodeConstantBoolean.get(!f.evaluate()));
    }

    @Override
    public String toString() {
        return "!(" + from + ")";
    }
}
