package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeImmutableString;

public class NodeAppendString implements INodeString {
    private final INodeString left, right;

    public NodeAppendString(INodeString left, INodeString right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String evaluate() {
        return left.evaluate() + right.evaluate();
    }

    @Override
    public INodeString inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, left, right, //
                (l, r) -> new NodeAppendString(l, r), //
                (l, r) -> new NodeImmutableString(l.evaluate() + r.evaluate()));
    }

    @Override
    public String toString() {
        return "(" + left + ") + (" + right + ")";
    }
}
