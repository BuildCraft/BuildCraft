package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeFuncDoubleDoubleToDouble implements INodeFuncDouble {

    public final IFuncDoubleDoubleToDouble function;
    private final StringFunctionTri stringFunction;

    public NodeFuncDoubleDoubleToDouble(IFuncDoubleDoubleToDouble function) {
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncDoubleDoubleToDouble(IFuncDoubleDoubleToDouble function, String fnString) {
        this(function, (a, b) -> "[" + a + ", " + b + "] " + fnString);
    }

    public NodeFuncDoubleDoubleToDouble(IFuncDoubleDoubleToDouble function, StringFunctionTri stringFunction) {
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[double, double -> double] {" + function.toString() + "}" : stringFunction.apply("{0}", "{1}");
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
        return new Func(stack.popDouble(), stack.popDouble(), function, stringFunction);
    }

    private static class Func implements INodeDouble {
        private final INodeDouble a, b;
        private final IFuncDoubleDoubleToDouble function;
        private final StringFunctionTri stringFunction;

        public Func(INodeDouble left, INodeDouble right, IFuncDoubleDoubleToDouble function, StringFunctionTri stringFunction) {
            this.a = right;
            this.b = left;
            this.function = function;
            this.stringFunction = stringFunction;
        }

        @Override
        public double evaluate() {
            return function.apply(a.evaluate(), b.evaluate());
        }

        @Override
        public INodeDouble inline() {
            return NodeInliningHelper.tryInline(this, a, b, (a, b) -> new Func(a, b, function, stringFunction),//
                    (a, b) -> new NodeConstantDouble(function.apply(a.evaluate(), b.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + a + ", " + b + " -> long] {" + function.toString() + "}"//
                : stringFunction.apply(a.toString(), b.toString());
        }
    }

    public interface IFuncDoubleDoubleToDouble {
        double apply(double a, double b);
    }
}
