package buildcraft.lib.expression.node.binary;

import com.google.common.base.Objects;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeImmutableBoolean;

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
        return Objects.equal(sa, sb);
    }

    @Override
    public INodeBoolean inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, a, b, //
                (l, r) -> new NodeEqualityString(l, r), //
                (l, r) -> NodeImmutableBoolean.get(Objects.equal(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + a + ") == (" + b + ")";
    }
}
