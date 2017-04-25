package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;

public interface IVariableNode extends IExpressionNode {
    /** Sets the current value of this node to be the value returned by the given node. Note that this does require a
     * cast. This should throw an {@link IllegalArgumentException} or a {@link ClassCastException} if the given node is
     * not of the correct type. */
    void set(IExpressionNode from);

    String valueToString();

    String getName();
}
