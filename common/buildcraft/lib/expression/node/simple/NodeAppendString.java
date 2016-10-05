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
        INodeString il = this.left.inline(args);
        INodeString ir = this.right.inline(args);

        if (il instanceof NodeValueString && ir instanceof NodeValueString) {
            return new NodeValueString(((NodeValueString) il).value + ((NodeValueString) ir).value);
        } else if (il == this.left && ir == this.right) {
            return this;
        } else {
            return new NodeAppendString(il, ir);
        }
    }
}
