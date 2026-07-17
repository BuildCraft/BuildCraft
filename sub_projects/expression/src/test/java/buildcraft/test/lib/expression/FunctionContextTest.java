package buildcraft.test.lib.expression;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.Argument;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.func.NodeFuncToLong.IFuncToLong;
import buildcraft.lib.expression.node.value.NodeVariableLong;

public class FunctionContextTest {
    @Test
    public void testPutAndGetVariable() {
        FunctionContext ctx = new FunctionContext("test");
        NodeVariableLong v = ctx.putVariableLong("myVar");
        v.value = 42;
        IExpressionNode got = ctx.getVariable("myVar");
        Assert.assertNotNull(got);
        Assert.assertTrue(got instanceof INodeLong);
        Assert.assertEquals(42, ((INodeLong) got).evaluate());
        Assert.assertTrue(ctx.hasLocalVariable("myVar"));
    }

    @Test
    public void testUnknownVariableReturnsNull() {
        FunctionContext ctx = new FunctionContext("test");
        Assert.assertNull(ctx.getVariable("nope"));
        Assert.assertFalse(ctx.hasLocalVariable("nope"));
    }

    @Test
    public void testPutConstant() {
        FunctionContext ctx = new FunctionContext("test");
        ctx.putConstantLong("rate", 6);
        IExpressionNode node = ctx.getVariable("rate");
        Assert.assertNotNull(node);
        Assert.assertEquals(6, ((INodeLong) node).evaluate());
    }

    @Test
    public void testNameCaseInsensitive() {
        FunctionContext ctx = new FunctionContext("test");
        ctx.putVariableLong("MyVar").value = 9;
        Assert.assertNotNull(ctx.getVariable("myvar"));
        Assert.assertNotNull(ctx.getVariable("MYVAR"));
        Assert.assertTrue(ctx.hasLocalVariable("myvar"));
    }

    @Test
    public void testUnknownFunctionReturnsNull() {
        FunctionContext ctx = new FunctionContext("test");
        Assert.assertNull(ctx.getFunction("nope", Collections.emptyList()));
    }

    @Test
    public void testPutAndGetFunction() throws InvalidExpressionException {
        FunctionContext ctx = new FunctionContext("test");
        INodeFunc f = GenericExpressionCompiler.compileFunctionLong("a + 1", ctx, Argument.argLong("a"));
        ctx.putFunction("inc", f);
        INodeFunc got = ctx.getFunction("inc", Collections.singletonList(long.class));
        Assert.assertNotNull(got);
        Assert.assertSame(f, got);
    }

    @Test
    public void testParentDelegation() {
        FunctionContext parent = new FunctionContext("parent");
        parent.putVariableLong("fromParent").value = 100;
        FunctionContext child = new FunctionContext("child", parent);
        IExpressionNode node = child.getVariable("fromParent");
        Assert.assertNotNull(node);
        Assert.assertEquals(100, ((INodeLong) node).evaluate());
        // not local to the child
        Assert.assertFalse(child.hasLocalVariable("fromParent"));
    }

    @Test
    public void testPutLambdaFunction() {
        FunctionContext ctx = new FunctionContext("test");
        // IFuncToLong is a 0-argument function (long apply()), stored under an empty arg list.
        IFuncToLong answer = () -> 42L;
        ctx.put_l("answer", answer);
        INodeFunc f = ctx.getFunction("answer", Collections.emptyList());
        Assert.assertNotNull(f);
    }

    @Test
    public void testGetAllVariables() {
        FunctionContext ctx = new FunctionContext("test");
        ctx.putVariableLong("a");
        ctx.putVariableDouble("b");
        Assert.assertTrue(ctx.getAllVariables().contains("a"));
        Assert.assertTrue(ctx.getAllVariables().contains("b"));
    }
}
