package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeImmutableBoolean;
import buildcraft.lib.expression.node.value.NodeImmutableString;

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
    public INodeString inline(Arguments args) {
        GenericExpressionCompiler.debugStart("Inlining " + this);
        INodeBoolean c = condition.inline(args);
        INodeString t = ifTrue.inline(args);
        INodeString f = ifFalse.inline(args);
        if (c instanceof NodeImmutableBoolean && t instanceof NodeImmutableString && f instanceof NodeImmutableString) {
            NodeImmutableString val = new NodeImmutableString(((NodeImmutableBoolean) c).value ? ((NodeImmutableString) t).value : ((NodeImmutableString) f).value);
            GenericExpressionCompiler.debugEnd("Fully inlined to " + val);
            return val;
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            NodeConditionalString val = new NodeConditionalString(c, t, f);
            GenericExpressionCompiler.debugEnd("Partially inlined to " + val);
            return val;
        } else {
            GenericExpressionCompiler.debugEnd("Unable to inline at all!");
            return this;
        }
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
