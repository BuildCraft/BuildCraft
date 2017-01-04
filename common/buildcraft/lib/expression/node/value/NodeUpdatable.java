package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;

public class NodeUpdatable {
    private final IExpressionNode source;
    public final IVariableNode variable;

    public NodeUpdatable(IExpressionNode source, IVariableNode variable) {
        this.source = source;
        this.variable = variable;
    }

    public void refresh() {
        variable.set(source);
    }
}
