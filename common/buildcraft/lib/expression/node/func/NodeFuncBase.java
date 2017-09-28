package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;

public abstract class NodeFuncBase implements INodeFunc {

    protected boolean canInline = true;

    /** Sets this function as one that relies on more factors than just the arguments given to it. As such
     * {@link IExpressionNode#inline()} will always return a function, rather than an {@link IConstantNode}. */
    public void setNeverInline() {
        canInline = false;
    }
}
