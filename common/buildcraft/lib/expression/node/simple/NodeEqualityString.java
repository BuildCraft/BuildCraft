package buildcraft.lib.expression.node.simple;

import com.google.common.base.Objects;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

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
        INodeString ia = a.inline(args);
        INodeString ib = b.inline(args);

        if (ia instanceof NodeValueString && ib instanceof NodeValueString) {
            return NodeValueBoolean.get(Objects.equal(ia.evaluate(), ib.evaluate()));
        } else if (ia == a && ib == b) {
            return this;
        } else {
            return new NodeEqualityString(ia, ib);
        }
    }
}
