package buildcraft.test.core.config;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import com.google.common.collect.Maps;

import org.junit.Test;

import buildcraft.lib.expression.Expression;
import buildcraft.lib.expression.ExpressionCompiler;
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

        bakeAndCallDouble("(49)^(1/2)-(2*3)", -5);// It only deals with longs, not doubles
        bakeAndCallDouble("2*(-3)", -6);
        bakeAndCallDouble("2*-3", -6);
    }

    @Test
    public void testFunctions() {
        Map<String, Expression> functions = Maps.newHashMap();
        functions.put("one", bakeFunction("1", functions));
        bakeAndCallDouble("one()", 1, functions);
        bakeAndCallDouble("oNe()", 1, functions);

        Expression same = bakeFunction("value", functions);
        functions.put("same", same);
        bakeAndCallDouble("same(0)", 0, functions);
        bakeAndCallDouble("same(one())", 1, functions);
        bakeAndCallDouble("same(2^5)", 32, functions);

        Expression powerTwo = bakeFunction("2^input", functions);
        functions.put("powertwo", powerTwo);
        bakeAndCallDouble("powerTwo(5)", 32, functions);
        bakeAndCallDouble("powertwo(6)", 64, functions);

        Expression subtract = bakeFunction("a-b", functions);
        functions.put("subtract", subtract);
        bakeAndCallDouble("subtract(3, 1)", 2, functions);
        bakeAndCallDouble("subtract(1, 3)", -2, functions);
        bakeAndCallDouble("subtract(1, -3)", 4, functions);
    }

    private static Expression bakeFunction(String function, Map<String, Expression> functions) {
        try {
            return ExpressionCompiler.compileExpression(function, functions);
        } catch (InvalidExpressionException iee) {
            throw new AssertionError(iee);
        }
    }

    private static void bakeAndCallDouble(String function, long def) {
        bakeAndCallDouble(function, def, null);
    }

    private static void bakeAndCallDouble(String function, long expected, Map<String, Expression> functions) {
        System.out.println("Testing \"" + function + "\", expecting " + expected);
        Expression func = bakeFunction(function, functions);
        long got = func.evaluate();
        assertEquals(expected, got);
    }
}
