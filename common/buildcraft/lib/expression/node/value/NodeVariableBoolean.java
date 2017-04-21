package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public class NodeVariableBoolean implements INodeBoolean, IVariableNode {
    public boolean value;
    private boolean isConst = false;

    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline() {
        if (isConst) {
            return NodeConstantBoolean.get(value);
        }
        return this;
    }

    @Override
    public String toString() {
        return "mutable_boolean#" + System.identityHashCode(this);
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeBoolean) from).evaluate();
    }

    @Override
    public String valueToString() {
        return Boolean.toString(value);
    }
}
