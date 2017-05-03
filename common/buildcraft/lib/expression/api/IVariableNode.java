package buildcraft.lib.expression.api;

public interface IVariableNode extends IExpressionNode {
    /** Sets the current value of this node to be the value returned by the given node. Note that this does require a
     * cast. This should throw an {@link IllegalArgumentException} or a {@link ClassCastException} if the given node is
     * not of the correct type. */
    void set(IExpressionNode from);

    /** If isConstant is true, then calls to {@link #inline()} will return an {@link IConstantNode} (which is
     * independant to this node), but if false then {@link #inline()} will return this variable. */
    void setConstant(boolean isConstant);

    String valueToString();

    String getName();

    public interface IVariableNodeLong extends IVariableNode, INodeLong {}

    public interface IVariableNodeDouble extends IVariableNode, INodeDouble {}

    public interface IVariableNodeBoolean extends IVariableNode, INodeBoolean {}

    public interface IVariableNodeString extends IVariableNode, INodeString {}
}
