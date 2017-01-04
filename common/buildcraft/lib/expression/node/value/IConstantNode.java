package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;

/** Marker interface that means calling evaluate() on this will *always* return the same value. */
public interface IConstantNode extends IExpressionNode {}
