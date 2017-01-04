package buildcraft.lib.expression.node.binary;

import java.util.Objects;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeEqualityString implements INodeBoolean {
    private final INodeString a, b;

    public NodeEqualityString(INodeString a, INodeString b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean evaluate() {
        String sa = a.evaluate();
        String sb = b.evaluate();
        return Objects.equals(sa, sb);
    }

    @Override
    public INodeBoolean inline() {
        return NodeInliningHelper.tryInline(this, a, b, (l, r) -> new NodeEqualityString(l, r),//
                (l, r) -> NodeConstantBoolean.get(Objects.equals(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + a + ") == (" + b + ")";
    }
}
