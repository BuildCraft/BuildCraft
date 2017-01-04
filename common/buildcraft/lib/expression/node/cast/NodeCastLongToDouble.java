package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

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
    public INodeDouble inline() {
        return NodeInliningHelper.tryInline(this, from, (f) -> new NodeCastLongToDouble(f), (f) -> new NodeConstantDouble(f.evaluate()));
    }

    @Override
    public String toString() {
        return "_long_to_double( " + from + " )";
    }
}
