package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantString;

public class NodeConcatenateString implements INodeString {
    private final INodeString left, right;

    public NodeConcatenateString(INodeString left, INodeString right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String evaluate() {
        return left.evaluate() + right.evaluate();
    }

    @Override
    public INodeString inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeConcatenateString(l, r), //
                (l, r) -> new NodeConstantString(l.evaluate() + r.evaluate()));
    }

    @Override
    public String toString() {
        return "(" + left + ") & (" + right + ")";
    }
}
