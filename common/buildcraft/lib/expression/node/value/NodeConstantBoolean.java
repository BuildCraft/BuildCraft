package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public enum NodeConstantBoolean implements INodeBoolean, IConstantNode {
    TRUE(true),
    FALSE(false);

    public final boolean value;

    private NodeConstantBoolean(boolean b) {
        this.value = b;
    }

    public static NodeConstantBoolean get(boolean value) {
        if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline() {
        return this;
    }

    public NodeConstantBoolean invert() {
        return get(!value);
    }
}
