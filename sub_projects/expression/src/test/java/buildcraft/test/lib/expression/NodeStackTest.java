package buildcraft.test.lib.expression;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.NodeStack;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeStackTest {
    @Test
    public void testPushPop() throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        Assert.assertTrue(stack.isEmpty());
        INodeLong a = stack.push(new NodeConstantLong(1));
        INodeDouble b = stack.push(new NodeConstantDouble(2.0));
        Assert.assertFalse(stack.isEmpty());
        Assert.assertSame(b, stack.pop());
        Assert.assertSame(a, stack.pop());
        Assert.assertTrue(stack.isEmpty());
    }

    @Test
    public void testPeek() throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        INodeLong a = stack.push(new NodeConstantLong(1));
        INodeDouble b = stack.push(new NodeConstantDouble(2.0));
        Assert.assertSame(b, stack.peek());
        Assert.assertFalse(stack.isEmpty());
        Assert.assertSame(b, stack.pop());
        Assert.assertSame(a, stack.pop());
    }

    @Test(expected = InvalidExpressionException.class)
    public void testPopEmptyThrows() throws InvalidExpressionException {
        new NodeStack().pop();
    }

    @Test(expected = InvalidExpressionException.class)
    public void testPeekEmptyThrows() throws InvalidExpressionException {
        new NodeStack().peek();
    }

    @Test
    public void testPeekCount() throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        INodeLong a = stack.push(new NodeConstantLong(1));
        INodeDouble b = stack.push(new NodeConstantDouble(2.0));
        // peek(count) returns nodes in the order they would be popped: index 0 is the top
        List<buildcraft.lib.expression.api.IExpressionNode> list = stack.peek(2);
        Assert.assertEquals(2, list.size());
        Assert.assertSame(b, list.get(0));
        Assert.assertSame(a, list.get(1));
    }

    @Test(expected = InvalidExpressionException.class)
    public void testPeekCountInsufficient() throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        stack.push(new NodeConstantLong(1));
        stack.peek(5);
    }

    @Test
    public void testTypedPops() throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        stack.push(new NodeConstantLong(7));
        stack.push(new NodeConstantDouble(3.5));
        stack.push(NodeConstantBoolean.of(true));
        INodeBoolean bool = stack.popBoolean();
        Assert.assertTrue(bool.evaluate());
        INodeDouble dbl = stack.popDouble();
        Assert.assertEquals(3.5, dbl.evaluate(), 0.0001);
        INodeLong lng = stack.popLong();
        Assert.assertEquals(7, lng.evaluate());
    }

    @Test(expected = InvalidExpressionException.class)
    public void testPopLongWrongType() throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        stack.push(new NodeConstantDouble(2.0));
        stack.popLong();
    }

    @Test(expected = InvalidExpressionException.class)
    public void testPopEmptyTyped() throws InvalidExpressionException {
        new NodeStack().popLong();
    }
}
