package buildcraft.test.lib.expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.Argument;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.NodeStack;
import buildcraft.lib.expression.VecLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public class ExpressionTester {
    static {
        ExpressionDebugManager.debug = true;
    }

    @Test
    public void testLongBasics() {
        bakeAndCallLong("0x0", 0x0);
        bakeAndCallLong("0xa", 0xa);
        bakeAndCallLong("0xA", 0xA);
        bakeAndCallLong("0x10", 0x10);
        bakeAndCallLong("0x1_0", 0x1_0);
    }

    @Test
    public void testDoubleBasics() {
        // I COULD change all these to be in separate functions... except that's really long :/
        bakeAndCallDouble("0", 0);
        bakeAndCallDouble("-1", -1);
        bakeAndCallDouble("0+1", 0 + 1);
        bakeAndCallDouble("   0   +    1    ", 1);
        bakeAndCallDouble("3-2", 3 - 2);
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

        bakeAndCallDouble("1 | 2", 3);
        bakeAndCallDouble("3 & 5", 1);
        bakeAndCallDouble("2*(-3)", -6);
        bakeAndCallDouble("2*-3", -6);

        bakeAndCallDouble("1 << 0", 1 << 0);
        bakeAndCallDouble("1 >> 0", 1 >> 0);
        bakeAndCallDouble("100 >> 2", 100 >> 2);
        bakeAndCallDouble("1 << 2", 1 << 2);
        bakeAndCallDouble("1 << 10", 1 << 10);
    }

    @Test
    public void testStringBasics() {
        bakeAndCallString("'a'", "a");
        bakeAndCallString("'A'", "A");
        bakeAndCallString("'a' + 'b'", "ab");
        bakeAndCallString("'aA' + 'b'", "aAb");
        bakeAndCallString("'aAB'.toLowerCase()", "aab");
        bakeAndCallString("'aAB'.tolOwercase()", "aab");

        bakeAndCallBoolean("'a' == 'a'", true);
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

        bakeAndCallDouble("false ? 1 : 0.4", 0.4);
    }

    @Test
    public void testMath() throws InvalidExpressionException {
        FunctionContext ctx2 = DefaultContexts.createWithAll();

        List<Class<?>> list_d = Collections.singletonList(double.class);
        List<Class<?>> list_l = Collections.singletonList(long.class);
        List<Class<?>> list_ll = Arrays.asList(long.class, long.class);
        System.out.println(ctx2.getFunctions("sin"));
        System.out.println(ctx2.getFunction("sin", list_d));
        System.out.println(ctx2.getFunction("cosh", list_d));
        System.out.println(ctx2.getFunction("round", list_d));
        System.out.println(ctx2.getFunction("ceil", list_d));
        System.out.println(ctx2.getFunction("max", list_d));
        System.out.println(ctx2.getFunction("max", list_l));
        System.out.println(ctx2.getFunction("max", list_ll));

        NodeStack stack4 = new NodeStack();

        stack4.push(new NodeConstantDouble(0.4));
        INodeLong out = (INodeLong) ctx2.getFunction("ceil", list_d).getNode(stack4);
        System.out.println(out + " = " + out.evaluate());

        stack4.push(new NodeConstantDouble(0.4));
        out = (INodeLong) ctx2.getFunction("floor", list_d).getNode(stack4);
        System.out.println(out + " = " + out.evaluate());

        INodeDouble nd = (INodeDouble) ctx2.getVariable("pi");
        System.out.println(nd + " = " + nd.evaluate());

        nd = (INodeDouble) ctx2.getVariable("e");
        System.out.println(nd + " = " + nd.evaluate());

        INodeFuncLong func3 =
            GenericExpressionCompiler.compileFunctionLong("input * 2 + 1", ctx2, Argument.argLong("input"));
        NodeStack stack3 = new NodeStack();
        NodeVariableLong input = stack3.push(new NodeVariableLong("input"));
        INodeLong node3 = func3.getNode(stack3);

        input.value = 1;
        System.out.println(node3 + " = " + node3.evaluate());

        input.value = 30;
        System.out.println(node3 + " = " + node3.evaluate());

        ctx2.put_ll_l("sub", (a, b) -> a - b);

        testExpr("floor(ceil(0.5)+0.5)", ctx2);
        testExpr("sub(5, 6)", ctx2);
        testExpr("5.sub(6.4.round()) + 0.5.ceil()", ctx2);
        testExpr("5.sub(6) + 0.5.ceil() & ' -- ' & 45 + 2", ctx2);
        testExpr("165 + 15 - 6 * 46.sub(10)", ctx2);
        testExpr("log(10)", ctx2);
        testExpr("log10(10)", ctx2);
        testExpr("cos(radians(90))", ctx2);
        testExpr("cos(radians(90)).round_float()", ctx2);
        testExpr("cos(radians(91)).round_float()", ctx2);
        testExpr("cos(radians(92)).round_float()", ctx2);
        testExpr("cos(radians(93)).round_float()", ctx2);
        testExpr("cos(radians(94)).round_float()", ctx2);

        testExpr("floor(ceil(0.5)+0.5)", ctx2);
        testExpr("sub(5, 6)", ctx2);
        testExpr("5.sub(6.4.round()) + 0.5.ceil()", ctx2);
        testExpr("5.sub(6) + 0.5.ceil() & ' -- ' & 45 + 2", ctx2);
        testExpr("165 + 15 - 6 * 46.sub(10)", ctx2);
        testExpr("log(10)", ctx2);
        testExpr("log10(10)", ctx2);
        testExpr("cos(radians(90))", ctx2);
        testExpr("cos(radians(90)).round_float()", ctx2);
        testExpr("cos(radians(91)).round_float()", ctx2);
        testExpr("cos(radians(92)).round_float()", ctx2);
        testExpr("cos(radians(93)).round_float()", ctx2);
        testExpr("cos(radians(94)).round_float()", ctx2);
    }

    @Test
    public void testFunctions() throws InvalidExpressionException {
        FunctionContext ctx = DefaultContexts.createWithAll();
        compileFuncLong(ctx, "one", "1");
        compileFuncLong(ctx, "same", "value", Argument.argLong("value"));

        compileFuncDouble(ctx, "same", "value", Argument.argDouble("value"));
        compileFuncDouble(ctx, "powertwo", "pow(2,input)", Argument.argDouble("input"));
        compileFuncDouble(ctx, "subtract", "l - r", Argument.argDouble("l"), Argument.argDouble("r"));
        compileFuncDouble(ctx, "tuple", "a + b + c", Argument.argDouble("a"), Argument.argDouble("b"),
            Argument.argDouble("c"));
        compileFuncDouble(ctx, "powlong", "pow((same(a + 1) - 1) , (same(b) * one()))", Argument.argDouble("a"),
            Argument.argDouble("b"));

        bakeAndCallDouble("one()", 1, ctx);
        bakeAndCallDouble("oNe()", 1, ctx);

        bakeAndCallDouble("same(0)", 0, ctx);
        bakeAndCallDouble("same(one())", 1, ctx);
        bakeAndCallDouble("same(pow(2,5))", 32, ctx);

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

    private static void compileFuncLong(FunctionContext ctx, String name, String expr, Argument... args)
        throws InvalidExpressionException {
        ctx.putFunction(name, GenericExpressionCompiler.compileFunctionLong(expr, ctx, args));
    }

    private static void compileFuncDouble(FunctionContext ctx, String name, String expr, Argument... args)
        throws InvalidExpressionException {
        ctx.putFunction(name, GenericExpressionCompiler.compileFunctionDouble(expr, ctx, args));
    }

    @Test
    public void testVariables() {
        FunctionContext ctx = new FunctionContext(DefaultContexts.createWithAll());

        NodeVariableDouble someVariable = ctx.putVariableDouble("something");
        someVariable.value = 0;
        bakeAndCallDouble("something", 0, ctx);
        someVariable.value = 1;
        bakeAndCallDouble("something", 1, ctx);

        NodeVariableObject<String> variant = ctx.putVariableString("variant");
        String exp = "variant == 'gold'";
        INodeBoolean expBool = bakeFunctionBoolean(exp, ctx);

        variant.value = "nether_brick";
        Assert.assertFalse(expBool.evaluate());
        variant.value = "gold";
        Assert.assertTrue(expBool.evaluate());
        variant.value = "iron";
        Assert.assertFalse(expBool.evaluate());

        exp = "variant == 'wood' ? 0 : variant == 'steel' ? 1 : variant == 'obsidian' ? 2 : 3";
        INodeLong expLong = bakeFunctionLong(exp, ctx);

        variant.value = "wood";
        Assert.assertEquals(expLong.evaluate(), 0);
        variant.value = "steel";
        Assert.assertEquals(expLong.evaluate(), 1);
        variant.value = "obsidian";
        Assert.assertEquals(expLong.evaluate(), 2);
        variant.value = "some_other_value";
        Assert.assertEquals(expLong.evaluate(), 3);
    }

    @Test
    public void testObjects() {
        FunctionContext ctx = new FunctionContext();

        ctx.putConstantLong("engine.rate", 6);
        bakeAndCallLong("engine.rate", 6, ctx);

        ctx.putConstantLong("engine.other_rate", 5);
        bakeAndCallBoolean("engine.rate != engine.other_rate", true, ctx);

        ctx.putConstant("engine.stage", String.class, "blue");
        bakeAndCallString("engine.stage.toUpperCase()", "BLUE", ctx);
    }

    @Test
    public void testVectors() {
        bakeAndCallString("VecLong.zero", VecLong.ZERO.toString());
        bakeAndCallString("vec(0, 0, 0, 0)", VecLong.ZERO.toString());
        bakeAndCallString("vec(3, 4) + vec(1, 2)", "{ 4, 6, 0, 0 }");
        bakeAndCallString("vec(3, 4) - vec(1, 2)", "{ 2, 2, 0, 0 }");
        bakeAndCallLong("vec(3, 4).dot2(vec(1, 2))", 11);
        bakeAndCallLong("vec(3, 4).dot3(vec(1, 2))", 11);
        bakeAndCallLong("vec(3, 4).dot4(vec(1, 2))", 11);
        bakeAndCallDouble("vec(3, 4).length()", Math.sqrt(3 * 3 + 4 * 4));
        bakeAndCallDouble("vec(3, 4).distanceTo(vec(3, 9))", 5);
    }

    private static INodeDouble bakeFunctionDouble(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionDouble(function, ctx);
        } catch (buildcraft.lib.expression.api.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallDouble(String function, double expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeDouble node = bakeFunctionDouble(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        double got = node.evaluate();
        Assert.assertEquals(expected, got, 0.0001);
    }

    private static void bakeAndCallDouble(String function, double def) {
        bakeAndCallDouble(function, def, DefaultContexts.createWithAll());
    }

    private static INodeBoolean bakeFunctionBoolean(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionBoolean(function, ctx);
        } catch (buildcraft.lib.expression.api.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallBoolean(String function, boolean expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeBoolean node = bakeFunctionBoolean(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        boolean got = node.evaluate();
        Assert.assertEquals(expected, got);
    }

    private static void bakeAndCallBoolean(String function, boolean def) {
        bakeAndCallBoolean(function, def, null);
    }

    private static INodeObject<String> bakeFunctionString(String function, FunctionContext ctx) {
        return bakeFunctionObject(String.class, function, ctx);
    }

    private static <T> INodeObject<T> bakeFunctionObject(Class<T> clazz, String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionObject(clazz, function, ctx);
        } catch (InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallString(String function, String expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeObject<String> node = bakeFunctionString(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        String got = node.evaluate();
        Assert.assertEquals(expected, got);
    }

    private static void bakeAndCallString(String function, String def) {
        bakeAndCallString(function, def, DefaultContexts.createWithAll());
    }

    private static INodeLong bakeFunctionLong(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionLong(function, ctx);
        } catch (buildcraft.lib.expression.api.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void testExpr(String expr, FunctionContext ctx) throws InvalidExpressionException {
        INodeObject<String> node = GenericExpressionCompiler.compileExpressionString(expr, ctx);
        System.out.println(expr + " = " + node.evaluate());
    }

    private static void bakeAndCallLong(String function, long expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeLong node = bakeFunctionLong(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        long got = node.evaluate();
        Assert.assertEquals(expected, got);
    }

    private static void bakeAndCallLong(String function, long def) {
        bakeAndCallLong(function, def, DefaultContexts.createWithAll());
    }
}
