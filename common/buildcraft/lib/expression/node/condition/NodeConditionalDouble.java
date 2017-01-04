package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

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
    public INodeDouble inline() {
        INodeBoolean c = condition.inline();
        INodeDouble t = ifTrue.inline();
        INodeDouble f = ifFalse.inline();
        if (c instanceof NodeConstantBoolean && t instanceof NodeConstantDouble && f instanceof NodeConstantDouble) {
            return new NodeConstantDouble(((NodeConstantBoolean) c).value ? ((NodeConstantDouble) t).value : ((NodeConstantDouble) f).value);
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
