package buildcraft.test.lib.expression;

import static buildcraft.lib.expression.Argument.*;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.*;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.binary.NodeBinaryLong;
import buildcraft.lib.expression.node.func.NodeFuncGenericToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong;
import buildcraft.lib.expression.node.unary.NodeUnaryLong;
import buildcraft.lib.expression.node.value.*;

@SuppressWarnings("static-method")
public class ExpressionTester {
    static {
        ExpressionDebugManager.debug = true;
    }

    @Test
    public void testLongBasics() {
        bakeAndCallLong("0x0", 0);
        bakeAndCallLong("0xa", 10);
        bakeAndCallLong("0xA", 10);
        bakeAndCallLong("0x10", 16);
        bakeAndCallLong("0x1_0", 16);
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
        bakeAndCallString("'A'", "A");
        bakeAndCallString("'a' + 'b'", "ab");
        bakeAndCallString("'aA' + 'b'", "aAb");

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
    }

    @Test
    public void testMath() throws InvalidExpressionException {
        NodeVariableLong arg1 = new NodeVariableLong();

        INodeLong node = NodeBinaryLong.Type.ADD.create(arg1, new NodeConstantLong(10));

        arg1.value = 1;
        System.out.println(node.evaluate());

        arg1.value = 6;
        System.out.println(node.evaluate());

        IVariableNode[] vars = { arg1 };

        NodeType[] ntArgs = { NodeType.LONG };

        NodeFuncGenericToLong func = new NodeFuncGenericToLong(node, ntArgs, vars);

        NodeStack nodeStack = new NodeStack();

        nodeStack.push(new NodeConstantLong(14));
        System.out.println(func.getNode(nodeStack).inline().evaluate());

        nodeStack.push(new NodeConstantLong(27));
        System.out.println(func.getNode(nodeStack).inline().evaluate());

        NodeFuncLongToLong func2 = new NodeFuncLongToLong((a) -> (a * 2), "a * 2");

        nodeStack.push(new NodeConstantLong(1));
        System.out.println(func2.getNode(nodeStack).inline().evaluate());

        nodeStack.push(new NodeConstantLong(13));
        System.out.println(func2.getNode(nodeStack).inline().evaluate());

        nodeStack.push(new NodeConstantLong(13));
        INodeLong neg = NodeUnaryLong.Type.NEG.create(func2.getNode(nodeStack));
        System.out.println(neg);
        INodeLong negInlined = neg.inline();
        System.out.println(negInlined);
        System.out.println(negInlined.evaluate());

        NodeStack stack = new NodeStack();

        stack.push(neg);

        NodeStackRecording recorder = new NodeStackRecording();

        ExpressionDebugManager.debugStart("Recording " + func + " [");
        func.getNode(recorder);
        ExpressionDebugManager.debugEnd("]");

        ExpressionDebugManager.debugStart("Compiling");
        stack.setRecorder(recorder.types, func);
        INodeLong node2 = func.getNode(stack);
        stack.checkAndRemoveRecorder();
        ExpressionDebugManager.debugEnd("Compiled as " + node2);
        ExpressionDebugManager.debugPrintln("Inlined as " + node2.inline());

        FunctionContext ctx2 = DefaultContexts.createWithAll();

        System.out.println(ctx2.getFunction("sin", 1));
        System.out.println(ctx2.getFunction("cosh", 1));
        System.out.println(ctx2.getFunction("round", 1));
        System.out.println(ctx2.getFunction("ceil", 1));
        System.out.println(ctx2.getFunction("max_long", 1));
        System.out.println(ctx2.getFunction("max_long", 2));

        NodeStack stack4 = new NodeStack();

        stack4.push(new NodeConstantDouble(0.4));
        INodeLong out = (INodeLong) ctx2.getFunction("ceil", 1).getNode(stack4);
        System.out.println(out + " = " + out.evaluate());

        stack4.push(new NodeConstantDouble(0.4));
        out = (INodeLong) ctx2.getFunction("floor", 1).getNode(stack4);
        System.out.println(out + " = " + out.evaluate());

        INodeDouble nd = (INodeDouble) ctx2.getVariable("pi");
        System.out.println(nd + " = " + nd.evaluate());

        nd = (INodeDouble) ctx2.getVariable("e");
        System.out.println(nd + " = " + nd.evaluate());

        INodeFuncLong func3 = GenericExpressionCompiler.compileFunctionLong("input * 2 + 1", ctx2, Argument.argLong("input"));
        NodeStack stack3 = new NodeStack();
        NodeVariableLong input = stack3.push(new NodeVariableLong());
        INodeLong node3 = func3.getNode(stack3);

        input.value = 1;
        System.out.println(node3 + " = " + node3.evaluate());

        input.value = 30;
        System.out.println(node3 + " = " + node3.evaluate());

        ctx2.putFunction("sub", new NodeFuncLongLongToLong((a, b) -> a - b, (a, b) -> a + " - " + b));
        ExpressionDebugManager.debug = false;

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
        FunctionContext ctx = new FunctionContext(DefaultContexts.CONTEXT_DEFAULT);
        compileFuncLong(ctx, "one", "1");
        compileFuncLong(ctx, "same", "value", argLong("value"));

        compileFuncDouble(ctx, "same", "value", argDouble("value"));
        compileFuncDouble(ctx, "powertwo", "2^input", argDouble("input"));
        compileFuncDouble(ctx, "subtract", "l - r", argDouble("l"), argDouble("r"));
        compileFuncDouble(ctx, "tuple", "a + b + c", argDouble("a"), argDouble("b"), argDouble("c"));
        compileFuncDouble(ctx, "powlong", "(same(a + 1) - 1) ^ (same(b) * one())", argDouble("a"), argDouble("b"));

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

    private static void compileFuncLong(FunctionContext ctx, String name, String expr, Argument... args) throws InvalidExpressionException {
        ctx.putFunction(name, GenericExpressionCompiler.compileFunctionLong(expr, ctx, args));
    }

    private static void compileFuncDouble(FunctionContext ctx, String name, String expr, Argument... args) throws InvalidExpressionException {
        ctx.putFunction(name, GenericExpressionCompiler.compileFunctionDouble(expr, ctx, args));
    }

    @Test
    public void testVariables() {
        FunctionContext ctx = new FunctionContext(DefaultContexts.CONTEXT_DEFAULT);

        NodeVariableDouble someVariable = ctx.putVariableDouble("something");
        someVariable.value = 0;
        bakeAndCallDouble("something", 0, ctx);
        someVariable.value = 1;
        bakeAndCallDouble("something", 1, ctx);

        NodeVariableString variant = ctx.putVariableString("variant");
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

    private static INodeDouble bakeFunctionDouble(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionDouble(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallDouble(String function, double expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeDouble node = bakeFunctionDouble(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        double got = node.evaluate();
        assertEquals(expected, got, 0.0001);
    }

    private static void bakeAndCallDouble(String function, double def) {
        bakeAndCallDouble(function, def, null);
    }

    private static INodeBoolean bakeFunctionBoolean(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionBoolean(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallBoolean(String function, boolean expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeBoolean node = bakeFunctionBoolean(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        boolean got = node.evaluate();
        assertEquals(expected, got);
    }

    private static void bakeAndCallBoolean(String function, boolean def) {
        bakeAndCallBoolean(function, def, null);
    }

    private static INodeString bakeFunctionString(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionString(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void bakeAndCallString(String function, String expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeString node = bakeFunctionString(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        String got = node.evaluate();
        assertEquals(expected, got);
    }

    private static void bakeAndCallString(String function, String def) {
        bakeAndCallString(function, def, null);
    }

    private static INodeLong bakeFunctionLong(String function, FunctionContext ctx) {
        try {
            return GenericExpressionCompiler.compileExpressionLong(function, ctx);
        } catch (buildcraft.lib.expression.InvalidExpressionException e) {
            throw new AssertionError(e);
        }
    }

    private static void testExpr(String expr, FunctionContext ctx) throws InvalidExpressionException {
        INodeString node = GenericExpressionCompiler.compileExpressionString(expr, ctx);
        System.out.println(expr + " = " + node.evaluate());
    }

    private static void bakeAndCallLong(String function, long expected, FunctionContext ctx) {
        ExpressionDebugManager.debugPrintln("Testing \"" + function + "\", expecting " + expected);
        INodeLong node = bakeFunctionLong(function, ctx);
        ExpressionDebugManager.debugPrintln("To " + node);
        long got = node.evaluate();
        assertEquals(expected, got);
    }

    private static void bakeAndCallLong(String function, long def) {
        bakeAndCallLong(function, def, null);
    }
}
