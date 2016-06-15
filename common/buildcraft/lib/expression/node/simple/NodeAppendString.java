package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

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
        INodeString left = this.left.inline(args);
        INodeString right = this.right.inline(args);

        if (left instanceof NodeValueString && right instanceof NodeValueString) {
            return new NodeValueString(((NodeValueString) left).value + ((NodeValueString) right).value);
        } else if (left == this.left && right == this.right) {
            return this;
        } else {
            return new NodeAppendString(left, right);
        }
    }
}
