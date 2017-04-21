package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeVariableDouble implements INodeDouble, IVariableNode {
    public double value;
    private boolean isConst = false;

    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public double evaluate() {
        return value;
    }

    @Override
    public INodeDouble inline() {
        if (isConst) {
            return new NodeConstantDouble(value);
        }
        return this;
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeDouble) from).evaluate();
    }

    @Override
    public String toString() {
        return "mutable_double#" + System.identityHashCode(this);
    }

    @Override
    public String valueToString() {
        double strVal = value * 1000;
        strVal = Math.round(strVal) / 1000.0;
        return Double.toString(strVal);
    }
}
