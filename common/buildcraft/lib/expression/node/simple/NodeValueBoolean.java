package buildcraft.lib.expression.node.simple;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public enum NodeValueBoolean implements INodeBoolean {
    TRUE(true),
    FALSE(false);

    public final boolean value;

    private NodeValueBoolean(boolean b) {
        this.value = b;
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline(Arguments args) {
        return this;
    }
}
