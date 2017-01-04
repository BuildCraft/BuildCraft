package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.node.value.IVariableNode;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeFuncGenericToLong extends NodeFuncGeneric implements INodeFuncLong {

    private final INodeLong node;

    public NodeFuncGenericToLong(INodeLong node, NodeType[] types, IVariableNode[] nodes) {
        super(node, types, nodes);
        this.node = node;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        return new FuncLong(popArgs(stack));
    }

    private class FuncLong extends Func implements INodeLong {
        public FuncLong(IExpressionNode[] argsIn) {
            super(argsIn);
        }

        @Override
        public long evaluate() {
            setupEvaluate(realArgs);
            return node.evaluate();
        }

        @Override
        public INodeLong inline() {
            IExpressionNode[] newArgs = new IExpressionNode[realArgs.length];
            InlineType type = setupInline(newArgs);
            if (type == InlineType.FULL) {
                setupEvaluate(newArgs);
                return new NodeConstantLong(node.evaluate());
            } else if (type == InlineType.PARTIAL) {
                return new FuncLong(newArgs);
            }
            return this;
        }
    }
}
