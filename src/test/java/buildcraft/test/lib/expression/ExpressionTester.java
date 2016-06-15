package buildcraft.test.lib.expression;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import buildcraft.lib.expression.api.IExpression.IExpressionDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.FunctionMap;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IFunctionMap;
import buildcraft.lib.expression.api.IFunctionMap.IInnerMap;

@SuppressWarnings("static-method")
public class ExpressionTester {

    @Test
    public void testBasics() {
        // I COULD change all these to be in separate functions... except that thats really long :/
        bakeAndCallDouble("0", 0);
        bakeAndCallDouble("-1", -1);
        bakeAndCallDouble("0+1", 1);
        bakeAndCallDouble("   0   +    1    ", 1);
        bakeAndCallDouble("3-2", 1);
        bakeAndCallDouble("1+1+1", 3);
        bakeAndCallDouble("1+2-1", 2);
        bakeAndCallDouble("1-2+1", 0);
        //
        bakeAndCallDouble("1-1", 0);
        bakeAndCallDouble("(1-1)", 0);
        bakeAndCallDouble("2--3", 5);
        bakeAndCallDouble("3--2", 5);
        bakeAndCallDouble("1-(-1)", 2);
        bakeAndCallDouble("-1-1", -2);
        bakeAndCallDouble("(-1)-1", -2);
        bakeAndCallDouble("1-(2+1)", -2);
        bakeAndCallDouble("(1)-(2+1)", -2);
        //
        bakeAndCallDouble("2^5", 32);
        bakeAndCallDouble("1+2^5*3", 97);

        bakeAndCallDouble("(49)^(-1/-2.0)-(2*3)", 1);
        bakeAndCallDouble("2*(-3)", -6);
        bakeAndCallDouble("2*-3", -6);
    }

    @Test
    public void testFunctions() {
        IFunctionMap functions = new FunctionMap();
        IInnerMap<IExpressionDouble> doubleMap = functions.getDoubleMap();

        doubleMap.putExpression("one", bakeFunction("1", functions));
        doubleMap.putExpression("same", bakeFunction("{long value} (value)", functions));
        doubleMap.putExpression("same", bakeFunction("{double value} (value)", functions));
        doubleMap.putExpression("powertwo", bakeFunction("{double input} (2^input)", functions));
        doubleMap.putExpression("subtract", bakeFunction("{double l, double r}   (l - r)", functions));
        doubleMap.putExpression("tuple", bakeFunction("{double a, double b, double c}  (a + b + c)", functions));

        bakeAndCallDouble("one()", 1, functions);
        bakeAndCallDouble("oNe()", 1, functions);

        bakeAndCallDouble("same(0)", 0, functions);
        bakeAndCallDouble("same(one())", 1, functions);
        bakeAndCallDouble("same(2^5)", 32, functions);

        bakeAndCallDouble("powerTwo(5)", 32, functions);
        bakeAndCallDouble("powertwo(6)", 64, functions);

        bakeAndCallDouble("subtract(3, 1)", 2, functions);
        bakeAndCallDouble("subtract(1, 3)", -2, functions);
        bakeAndCallDouble("subtract(1, -3)", 4, functions);
        bakeAndCallDouble("subtract(1, -3)", 4, functions);

        bakeAndCallDouble("tuple(1, 2, 3)", 6, functions);
        bakeAndCallDouble("tuple(3, 2, 1)", 6, functions);
        bakeAndCallDouble("tuple(-7, 1, 0)", -6, functions);
        bakeAndCallDouble("tuple(1, 3, 2)", 6, functions);
    }

    private static IExpressionDouble bakeFunction(String function, IFunctionMap functions) {
        try {
            return GenericExpressionCompiler.compileExpressionDouble(function, functions);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallDouble(String function, double def) {
        bakeAndCallDouble(function, def, null);
    }

    private static void bakeAndCallDouble(String function, double expected, IFunctionMap functions) {
        System.out.println("Testing \"" + function + "\", expecting " + expected);
        IExpressionDouble func = bakeFunction(function, functions);
        Arguments args = Arguments.NO_ARGS;
        System.out.println("From " + func);
        INodeDouble node = func.derive(args);
        System.out.println("To " + node);
        double got = node.evaluate();
        assertEquals(expected, got, 0.0001);
    }
}
