package buildcraft.test.lib.expression;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.FunctionMap;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpression.IExpressionBoolean;
import buildcraft.lib.expression.api.IExpression.IExpressionDouble;
import buildcraft.lib.expression.api.IExpression.IExpressionLong;
import buildcraft.lib.expression.api.IExpression.IExpressionString;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.IFunctionMap;
import buildcraft.lib.expression.api.IFunctionMap.IInnerMap;
import buildcraft.lib.expression.node.value.NodeMutableDouble;
import buildcraft.lib.expression.node.value.NodeMutableString;

@SuppressWarnings("static-method")
public class ExpressionTester {

    static {
        GenericExpressionCompiler.debug = true;
    }

    @Test
    public void testDoubleBasics() {
        // I COULD change all these to be in separate functions... except that thats really long :/
        bakeAndCallDouble("0", 0);
        bakeAndCallDouble("-1", -1);
        bakeAndCallDouble("0+1", 1);
        bakeAndCallDouble("   0   +    1    ", 1);
        bakeAndCallDouble("3-2", 1);
        bakeAndCallDouble("1+1+1", 3);
        bakeAndCallDouble("1+2-1", 2);
        bakeAndCallDouble("1-2+1", 0);

        bakeAndCallDouble("1-1", 0);
        bakeAndCallDouble("(1-1)", 0);
        bakeAndCallDouble("2--3", 5);
        bakeAndCallDouble("3--2", 5);
        bakeAndCallDouble("1-(-1)", 2);
        bakeAndCallDouble("-1-1", -2);
        bakeAndCallDouble("(-1)-1", -2);
        bakeAndCallDouble("1-(2+1)", -2);
        bakeAndCallDouble("(1)-(2+1)", -2);

        bakeAndCallDouble("2^5", 32);
        bakeAndCallDouble("1+2^5*3", 97);

        bakeAndCallDouble("(49)^(-1/-2.0)-(2*3)", 1);
        bakeAndCallDouble("2*(-3)", -6);
        bakeAndCallDouble("2*-3", -6);
    }

    @Test
    public void testStringBasics() {
        bakeAndCallString("'a'", "a");
        bakeAndCallString("'A'", "a");
        bakeAndCallString("'a' + 'b'", "ab");
        bakeAndCallString("'aA' + 'b'", "aab");

        bakeAndCallBoolean("'a' != 'b'", true);
    }

    @Test
    public void testBooleanBasics() {
        bakeAndCallBoolean("true", true);
        bakeAndCallBoolean("!true", false);
        bakeAndCallBoolean("1 == 2", false);
        bakeAndCallBoolean("1 <= 2", true);
        bakeAndCallBoolean("1 == 1", true);
        bakeAndCallBoolean("1 != 1", false);
        bakeAndCallBoolean("1 == 1 || 1 > 2", true);
        bakeAndCallBoolean("1 == 1 && 1 > 2", false);

        bakeAndCallString("true ? 'hi' : 'nope'", "hi");
        bakeAndCallString("true ? 'h'+'i' : 'no'+'pe'", "hi");
        bakeAndCallString("false ? 'hi' : 'nope'", "nope");
        bakeAndCallString("1 <= 5^2-1 ? 'larger' : 'smaller'", "larger");

        bakeAndCallLong("false ? 0 : true ? 1 : 2", 1);
        bakeAndCallLong("(true ? false : true) ? 0 : 1", 1);
        bakeAndCallLong("(false ? 0 : 2) - 1", 1);
    }

    @Test
    public void testFunctions() {
        IFunctionMap functions = new FunctionMap();
        IInnerMap<IExpressionDouble> doubleMap = functions.getDoubleMap();
        FunctionContext ctx = new FunctionContext(functions);

        doubleMap.putExpression("one", bakeFunctionDouble("1", ctx));
        doubleMap.putExpression("same", bakeFunctionDouble("{long value} (value)", ctx));
        doubleMap.putExpression("same", bakeFunctionDouble("{double value} (value)", ctx));
        doubleMap.putExpression("powertwo", bakeFunctionDouble("{double input} (2^input)", ctx));
        doubleMap.putExpression("subtract", bakeFunctionDouble("{double l, double r} (l - r)", ctx));
        doubleMap.putExpression("tuple", bakeFunctionDouble("{double a, double b, double c} (a + b + c)", ctx));
        doubleMap.putExpression("powlong", bakeFunctionDouble("{double a, double b} (same(a + 1) - 1) ^ (same(b) * one())", ctx));

        bakeAndCallDouble("one()", 1, ctx);
        bakeAndCallDouble("oNe()", 1, ctx);

        bakeAndCallDouble("same(0)", 0, ctx);
        bakeAndCallDouble("same(one())", 1, ctx);
        bakeAndCallDouble("same(2^5)", 32, ctx);

        bakeAndCallDouble("powerTwo(5)", 32, ctx);
        bakeAndCallDouble("powertwo(6)", 64, ctx);

        bakeAndCallDouble("subtract(3, 1)", 2, ctx);
        bakeAndCallDouble("subtract(1, 3)", -2, ctx);
        bakeAndCallDouble("subtract(1, -3)", 4, ctx);
        bakeAndCallDouble("subtract(1, -3)", 4, ctx);

        bakeAndCallDouble("tuple(1, 2, 3)", 6, ctx);
        bakeAndCallDouble("tuple(3, 2, 1)", 6, ctx);
        bakeAndCallDouble("tuple(-7, 1, 0)", -6, ctx);
        bakeAndCallDouble("tuple(1, 3, 2)", 6, ctx);

        bakeAndCallDouble("powLong(3, 3)", 27, ctx);
    }

    @Test
    public void testVariables() {
        IFunctionMap functions = new FunctionMap();
        FunctionContext ctx = new FunctionContext(functions);

        NodeMutableDouble someVariable = ctx.getOrAddDouble("something");
        someVariable.value = 0;
        bakeAndCallDouble("something", 0, ctx);
        someVariable.value = 1;
        bakeAndCallDouble("something", 1, ctx);

        NodeMutableString variant = ctx.getOrAddString("variant");
        String exp = "variant == 'gold'";
        INodeBoolean expBool = bakeFunctionBoolean(exp, ctx).derive(null);

        variant.value = "nether_brick";
        Assert.assertFalse(expBool.evaluate());
        variant.value = "gold";
        Assert.assertTrue(expBool.evaluate());
        variant.value = "iron";
        Assert.assertFalse(expBool.evaluate());

        exp = "variant == 'wood' ? 0 : variant == 'steel' ? 1 : variant == 'obsidian' ? 2 : 3";
        INodeLong expLong = bakeFunctionLong(exp, ctx).derive(null);

        variant.value = "wood";
        Assert.assertEquals(expLong.evaluate(), 0);
        variant.value = "steel";
        Assert.assertEquals(expLong.evaluate(), 1);
        variant.value = "obsidian";
        Assert.assertEquals(expLong.evaluate(), 2);
        variant.value = "some_other_value";
        Assert.assertEquals(expLong.evaluate(), 3);
    }

    private static IExpressionDouble bakeFunctionDouble(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionDouble(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallDouble(String function, double expected, FunctionContext ctx) {
        GenericExpressionCompiler.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        IExpressionDouble func = bakeFunctionDouble(function, ctx);
        Arguments args = Arguments.NO_ARGS;
        GenericExpressionCompiler.debugPrintln("From " + func);
        INodeDouble node = func.derive(args);
        GenericExpressionCompiler.debugPrintln("To " + node);
        double got = node.evaluate();
        assertEquals(expected, got, 0.0001);
    }

    private static void bakeAndCallDouble(String function, double def) {
        bakeAndCallDouble(function, def, null);
    }

    private static IExpressionBoolean bakeFunctionBoolean(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionBoolean(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallBoolean(String function, boolean expected, FunctionContext ctx) {
        GenericExpressionCompiler.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        IExpressionBoolean func = bakeFunctionBoolean(function, ctx);
        Arguments args = Arguments.NO_ARGS;
        GenericExpressionCompiler.debugPrintln("From " + func);
        INodeBoolean node = func.derive(args);
        GenericExpressionCompiler.debugPrintln("To " + node);
        boolean got = node.evaluate();
        assertEquals(expected, got);
    }

    private static void bakeAndCallBoolean(String function, boolean def) {
        bakeAndCallBoolean(function, def, null);
    }

    private static IExpressionString bakeFunctionString(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionString(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallString(String function, String expected, FunctionContext ctx) {
        GenericExpressionCompiler.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        IExpressionString func = bakeFunctionString(function, ctx);
        Arguments args = Arguments.NO_ARGS;
        GenericExpressionCompiler.debugPrintln("From " + func);
        INodeString node = func.derive(args);
        GenericExpressionCompiler.debugPrintln("To " + node);
        String got = node.evaluate();
        assertEquals(expected, got);
    }

    private static void bakeAndCallString(String function, String def) {
        bakeAndCallString(function, def, null);
    }

    private static IExpressionLong bakeFunctionLong(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionLong(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallLong(String function, long expected, FunctionContext ctx) {
        GenericExpressionCompiler.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        IExpressionLong func = bakeFunctionLong(function, ctx);
        Arguments args = Arguments.NO_ARGS;
        GenericExpressionCompiler.debugPrintln("From " + func);
        INodeLong node = func.derive(args);
        GenericExpressionCompiler.debugPrintln("To " + node);
        long got = node.evaluate();
        assertEquals(expected, got);
    }

    private static void bakeAndCallLong(String function, long def) {
        bakeAndCallLong(function, def, null);
    }
}
