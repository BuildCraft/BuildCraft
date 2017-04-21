package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;

public interface ITickableNode {
    /** Called at any time */
    void refresh();

    /** Called once every minecraft tick. Used for variables that can changed depending on their previous value. */
    void tick();

    public interface Source {
        ITickableNode createTickable();

        void setSource(IExpressionNode node);
    }
}
