package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public class NodeVariableString implements INodeString, IVariableNode {
    public String value = "";
    private boolean isConst = false;

    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public String evaluate() {
        return value;
    }

    @Override
    public INodeString inline() {
        if (isConst) {
            return new NodeConstantString(value);
        }
        return this;
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeString) from).evaluate();
    }

    @Override
    public String toString() {
        return "mutable_string#" + System.identityHashCode(this);
    }

    @Override
    public String valueToString() {
        return value;
    }
}
