package buildcraft.test.lib.expression;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.Argument;
import buildcraft.lib.expression.FunctionSignature;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class FunctionSignatureTest {
    @Test
    public void testNoArgsNoBody() throws InvalidExpressionException {
        FunctionSignature sig = FunctionSignature.parse("myFunc()");
        Assert.assertEquals("myFunc", sig.name);
        Assert.assertEquals(0, sig.args.length);
        Assert.assertNull(sig.func);
    }

    @Test
    public void testSingleArg() throws InvalidExpressionException {
        FunctionSignature sig = FunctionSignature.parse("add(long a)");
        Assert.assertEquals("add", sig.name);
        Assert.assertEquals(1, sig.args.length);
        Assert.assertEquals("a", sig.args[0].name);
        Assert.assertSame(long.class, sig.args[0].type);
        Assert.assertNull(sig.func);
    }

    @Test
    public void testTwoArgs() throws InvalidExpressionException {
        FunctionSignature sig = FunctionSignature.parse("combine(long a, double b)");
        Assert.assertEquals(2, sig.args.length);
        Assert.assertSame(long.class, sig.args[0].type);
        Assert.assertSame(double.class, sig.args[1].type);
    }

    // The parser does NOT support a "{ body }" form: passing one throws IllegalStateException
    // (state 6 has no handler - the substring-based body extraction below it is effectively dead code).
    @Test(expected = IllegalStateException.class)
    public void testBodyFormUnsupported() throws InvalidExpressionException {
        FunctionSignature.parse("inc(long a) { a + 1 }");
    }

    @Test(expected = InvalidExpressionException.class)
    public void testMissingParens() throws InvalidExpressionException {
        FunctionSignature.parse("myFunc");
    }

    @Test(expected = InvalidExpressionException.class)
    public void testUnclosedParens() throws InvalidExpressionException {
        FunctionSignature.parse("myFunc(");
    }

    @Test(expected = InvalidExpressionException.class)
    public void testMissingArgName() throws InvalidExpressionException {
        FunctionSignature.parse("myFunc(long)");
    }

    @Test(expected = InvalidExpressionException.class)
    public void testUnclosedAfterArgs() throws InvalidExpressionException {
        FunctionSignature.parse("myFunc(long a");
    }
}
