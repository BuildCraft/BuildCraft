package buildcraft.test.lib.expression;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class ExpressionErrorTest {
    @Test
    public void testValidStillWorks() throws InvalidExpressionException {
        INodeLong node = GenericExpressionCompiler.compileExpressionLong("1+2", DefaultContexts.createWithAll());
        Assert.assertEquals(3, node.evaluate());
    }

    @Test(expected = InvalidExpressionException.class)
    public void testTrailingOperator() throws InvalidExpressionException {
        GenericExpressionCompiler.compileExpressionLong("1 +", DefaultContexts.createWithAll());
    }

    @Test(expected = InvalidExpressionException.class)
    public void testEmpty() throws InvalidExpressionException {
        GenericExpressionCompiler.compileExpressionLong("", DefaultContexts.createWithAll());
    }

    @Test(expected = InvalidExpressionException.class)
    public void testUnbalancedParens() throws InvalidExpressionException {
        GenericExpressionCompiler.compileExpressionLong("(1+2", DefaultContexts.createWithAll());
    }

    @Test(expected = InvalidExpressionException.class)
    public void testUnknownFunction() throws InvalidExpressionException {
        GenericExpressionCompiler.compileExpressionLong("notARealFunction(1)", DefaultContexts.createWithAll());
    }
}
