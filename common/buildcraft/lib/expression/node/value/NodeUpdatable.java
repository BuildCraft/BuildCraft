package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;

public class NodeUpdatable {
    public final IVariableNode variable;
    private IExpressionNode source;

    public NodeUpdatable(IExpressionNode source, IVariableNode variable) {
        this.source = source;
        this.variable = variable;
    }

    public void refresh() {
        variable.set(source);
    }

    public void setSource(IExpressionNode source) {
        this.source = source;
    }
}
