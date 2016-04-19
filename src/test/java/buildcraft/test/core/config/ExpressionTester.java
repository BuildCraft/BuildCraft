package buildcraft.test.core.config;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import buildcraft.lib.expression.ExpressionCompiler;
import buildcraft.lib.expression.Expression;
import buildcraft.lib.expression.ExpressionCompiler.InvalidExpressionException;

public class ExpressionTester {

    @Test
    public void testBasics() {
        // I COULD change all these to be in separate functions... except that thats really long :/
        bakeAndCallDouble("0", 0);
        bakeAndCallDouble("-1", -1);
        bakeAndCallDouble("0+1", 1);
        bakeAndCallDouble("   0   +    1    ", 1);
        bakeAndCallDouble("3-1", 2);
        bakeAndCallDouble("1+1+1", 3);
        bakeAndCallDouble("1+2-1", 2);
        bakeAndCallDouble("1-2+1", 0);
        //
        bakeAndCallDouble("1-1", 0);
        bakeAndCallDouble("1--1", 2);
        bakeAndCallDouble("1-(2+1)", -2);
        bakeAndCallDouble("(1)-(2+1)", -2);
        //
        bakeAndCallDouble("2^5", 32);
        bakeAndCallDouble("1+2^5*3", 97);
        bakeAndCallDouble("(49)^(1/2)-(2*3)", 1);
        bakeAndCallDouble("2*(-3)", -6);
    }

    // @Test
    // public void testFunctions() {
    // Map<String, Expression> functions = Maps.newHashMap();
    // functions.put("one", new BakedFunctionConstant<Double>(1.0));
    // bakeAndCallDouble("one", 1, functions);
    // bakeAndCallDouble("oNe", 1, functions);
    //
    // BakedFunction<Double> same = FunctionBaker.bakeFunction("{0}", functions, new String[1]);
    // functions.put("same", same);
    // bakeAndCallDouble("same(0)", 0, functions);
    // bakeAndCallDouble("same(one)", 1, functions);
    // bakeAndCallDouble("same(2^5)", 32, functions);
    //
    // BakedFunction<Double> powerTwo = FunctionBaker.bakeFunction("2^{0}", functions, new String[1]);
    // functions.put("powertwo", powerTwo);
    // bakeAndCallDouble("powerTwo(5)", 32, functions);
    // bakeAndCallDouble("powertwo(6)", 64, functions);
    //
    // BakedFunction<Double> subtract = FunctionBaker.bakeFunction("{0}-{1}", functions, new String[2]);
    // functions.put("subtract", subtract);
    // bakeAndCallDouble("subtract(3, 1)", 2, functions);
    // bakeAndCallDouble("subtract(1, 3)", -2, functions);
    // }

    private static void bakeAndCallDouble(String function, long def) {
        bakeAndCallDouble(function, def, null);
    }

    private static void bakeAndCallDouble(String function, long expected, Map<String, Expression> functions) {
        System.out.println("Testing " + function + ", expecting " + expected);
        try {
            Expression func = ExpressionCompiler.compileExpression(function, functions);
            long got = func.evaluate();
            assertEquals(expected, got);
        } catch (InvalidExpressionException iee) {
            throw new AssertionError(iee);
        }
    }
}
