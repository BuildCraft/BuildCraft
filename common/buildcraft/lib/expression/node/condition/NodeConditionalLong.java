package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeImmutableBoolean;
import buildcraft.lib.expression.node.value.NodeImmutableLong;

public class NodeConditionalLong implements INodeLong {
    private final INodeBoolean condition;
    private final INodeLong ifTrue, ifFalse;

    public NodeConditionalLong(INodeBoolean condition, INodeLong ifTrue, INodeLong ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public long evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeLong inline(Arguments args) {
        INodeBoolean c = condition.inline(args);
        INodeLong t = ifTrue.inline(args);
        INodeLong f = ifFalse.inline(args);
        if (c instanceof NodeImmutableBoolean && t instanceof NodeImmutableLong && f instanceof NodeImmutableLong) {
            return new NodeImmutableLong(((NodeImmutableBoolean) c).value ? ((NodeImmutableLong) t).value : ((NodeImmutableLong) f).value);
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            return new NodeConditionalLong(c, t, f);
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
