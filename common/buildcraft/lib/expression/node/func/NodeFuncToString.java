package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncString;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToString implements INodeFuncString, INodeString {

    private final String name;
    private final IFuncToString func;

    public NodeFuncToString(String name, IFuncToString func) {
        this.name = name;
        this.func = func;
    }

    @Override
    public String evaluate() {
        return func.apply();
    }

    @Override
    public INodeString inline() {
        return this;
    }

    @Override
    public INodeString getNode(INodeStack stack) throws InvalidExpressionException {
        return this;
    }

    @Override
    public String toString() {
        return "[ -> boolean] { " + name + " }";
    }

    public interface IFuncToString {
        String apply();
    }
}
