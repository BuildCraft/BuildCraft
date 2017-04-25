package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeType;

public class NodeUpdatable implements ITickableNode, ITickableNode.Source {
    public final String name;
    public final IVariableNode variable;
    private IExpressionNode source;

    public NodeUpdatable(String name, IExpressionNode source) {
        this.name = name;
        this.source = source;
        this.variable = NodeType.getType(source).makeVariableNode(name);
    }

    @Override
    public void refresh() {
        variable.set(source);
    }

    @Override
    public void tick() {
        refresh();
    }

    @Override
    public ITickableNode createTickable() {
        return this;
    }

    @Override
    public void setSource(IExpressionNode source) {
        this.source = source;
    }
}
