package buildcraft.lib.expression.test;

import buildcraft.lib.expression.*;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.node.binary.NodeBinaryLong;
import buildcraft.lib.expression.node.func.NodeFuncGenericToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong;
import buildcraft.lib.expression.node.unary.NodeUnaryLong;
import buildcraft.lib.expression.node.value.IVariableNode;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeVariableLong;

public class SimpleTester {

    public static void main(String[] args) throws InvalidExpressionException {
        long start = System.currentTimeMillis();

        // GenericToLong tests:

        ExpressionDebugManager.debug = true;

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

        long now = System.currentTimeMillis();
        System.out.println("Took " + (now - start) + "ms");
        start = now;

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

        System.out.println("Took " + (now - start) + "ms");
    }

    private static void testExpr(String expr, FunctionContext ctx) throws InvalidExpressionException {
        INodeString node = GenericExpressionCompiler.compileExpressionString(expr, ctx);
        System.out.println(expr + " = " + node.evaluate());
    }
}
