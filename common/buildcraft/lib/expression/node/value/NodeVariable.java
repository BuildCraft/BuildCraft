package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IVariableNode;

public abstract class NodeVariable implements IVariableNode {

    public final String name;
    protected boolean isConst = false;

    public NodeVariable(String name) {
        this.name = name;
    }

    @Override
    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public String toString() {
        return name + " = " + evaluateAsString();
    }

    @Override
    public String getName() {
        return name;
    }
}
