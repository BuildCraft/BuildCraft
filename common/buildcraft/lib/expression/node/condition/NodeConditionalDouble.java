package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeImmutableBoolean;
import buildcraft.lib.expression.node.value.NodeImmutableDouble;

public class NodeConditionalDouble implements INodeDouble {
    private final INodeBoolean condition;
    private final INodeDouble ifTrue, ifFalse;

    public NodeConditionalDouble(INodeBoolean condition, INodeDouble ifTrue, INodeDouble ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public double evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeDouble inline(Arguments args) {
        INodeBoolean c = condition.inline(args);
        INodeDouble t = ifTrue.inline(args);
        INodeDouble f = ifFalse.inline(args);
        if (c instanceof NodeImmutableBoolean && t instanceof NodeImmutableDouble && f instanceof NodeImmutableDouble) {
            return new NodeImmutableDouble(((NodeImmutableBoolean) c).value ? ((NodeImmutableDouble) t).value : ((NodeImmutableDouble) f).value);
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            return new NodeConditionalDouble(c, t, f);
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
