package buildcraft.test.lib.expression;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.Argument;

public class ArgumentTest {
    @Test
    public void testArgLong() {
        Argument a = Argument.argLong("input");
        Assert.assertEquals("input", a.name);
        Assert.assertSame(long.class, a.type);
    }

    @Test
    public void testArgDouble() {
        Argument a = Argument.argDouble("value");
        Assert.assertEquals("value", a.name);
        Assert.assertSame(double.class, a.type);
    }

    @Test
    public void testArgBoolean() {
        Argument a = Argument.argBoolean("flag");
        Assert.assertSame(boolean.class, a.type);
    }

    @Test
    public void testArgString() {
        Argument a = Argument.argString("text");
        Assert.assertSame(String.class, a.type);
    }

    @Test
    public void testArgObject() {
        Argument a = Argument.argObject("obj", Integer.class);
        Assert.assertSame(Integer.class, a.type);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("string 'foo'", new Argument("foo", String.class).toString());
        Assert.assertEquals("long 'bar'", new Argument("bar", long.class).toString());
    }
}
