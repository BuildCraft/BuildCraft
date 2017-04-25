package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeVariableLong implements INodeLong, IVariableNode {
    public final String name;
    public long value;
    private boolean isConst = false;

    public NodeVariableLong(String name) {
        this.name = name;
    }

    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public long evaluate() {
        return value;
    }

    @Override
    public INodeLong inline() {
        if (isConst) {
            return new NodeConstantLong(value);
        }
        return this;
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeLong) from).evaluate();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String valueToString() {
        return Long.toString(value);
    }
}
