package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IVariableNode;

public abstract class NodeVariable implements IVariableNode {

    public final String name;
    protected boolean isConst = false;

    public NodeVariable(String name) {
        this.name = name;
    }

    /** If isConstant is true, then calls to {@link #inline()} will return an {@link IConstantNode} (which is
     * independent to this node), but if false then {@link #inline()} will return this variable. */
    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public String toString() {
        return "variable: " + name + " = " + evaluateAsString();
    }
}
