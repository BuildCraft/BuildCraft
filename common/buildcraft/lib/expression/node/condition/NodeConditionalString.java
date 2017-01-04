package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantString;

public class NodeConditionalString implements INodeString {
    private final INodeBoolean condition;
    private final INodeString ifTrue, ifFalse;

    public NodeConditionalString(INodeBoolean condition, INodeString ifTrue, INodeString ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public String evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeString inline() {
        ExpressionDebugManager.debugStart("Inlining " + this);
        INodeBoolean c = condition.inline();
        INodeString t = ifTrue.inline();
        INodeString f = ifFalse.inline();
        if (c instanceof NodeConstantBoolean && t instanceof NodeConstantString && f instanceof NodeConstantString) {
            NodeConstantString val = new NodeConstantString(((NodeConstantBoolean) c).value ? ((NodeConstantString) t).value : ((NodeConstantString) f).value);
            ExpressionDebugManager.debugEnd("Fully inlined to " + val);
            return val;
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            NodeConditionalString val = new NodeConditionalString(c, t, f);
            ExpressionDebugManager.debugEnd("Partially inlined to " + val);
            return val;
        } else {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return this;
        }
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
