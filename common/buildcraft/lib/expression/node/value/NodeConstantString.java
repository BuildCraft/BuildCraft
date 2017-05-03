package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public class NodeConstantString implements INodeString, IConstantNode {
    public static final NodeConstantString EMPTY = new NodeConstantString("");
    
    public final String value;

    public NodeConstantString(String value) {
        this.value = value;
    }

    @Override
    public String evaluate() {
        return value;
    }

    @Override
    public INodeString inline() {
        return this;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
}
