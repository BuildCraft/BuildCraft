package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

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
    public INodeBoolean inline(Arguments args) {
        INodeBoolean inlined = this.from.inline(args);
        if (inlined instanceof NodeValueBoolean) {
            return ((NodeValueBoolean) inlined).invert();
        } else if (inlined == this.from) {
            return this;
        } else {
            return new NodeBooleanInvert(inlined);
        }
    }
}
