package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncString;
import buildcraft.lib.expression.node.value.IVariableNode;
import buildcraft.lib.expression.node.value.NodeConstantString;

public class NodeFuncGenericToString extends NodeFuncGeneric implements INodeFuncString {

    protected final INodeString node;

    public NodeFuncGenericToString(INodeString node, NodeType[] types, IVariableNode[] nodes) {
        super(node, types, nodes);
        this.node = node;
    }

    @Override
    public INodeString getNode(INodeStack stack) throws InvalidExpressionException {
        return new FuncString(popArgs(stack));
    }

    protected class FuncString extends Func implements INodeString {
        public FuncString(IExpressionNode[] argsIn) {
            super(argsIn);
        }

        @Override
        public String evaluate() {
            setupEvaluate(realArgs);
            return node.evaluate();
        }

        @Override
        public INodeString inline() {
            IExpressionNode[] newArgs = new IExpressionNode[realArgs.length];
            InlineType type = setupInline(newArgs);
            if (type == InlineType.FULL) {
                setupEvaluate(newArgs);
                return new NodeConstantString(node.evaluate());
            } else if (type == InlineType.PARTIAL) {
                return new FuncString(newArgs);
            }
            return this;
        }
    }
}
