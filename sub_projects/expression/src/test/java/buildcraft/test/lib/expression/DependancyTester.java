package buildcraft.test.lib.expression;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.info.DependencyVisitorCollector;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;

public class DependancyTester {

    static {
        ExpressionDebugManager.debug = true;
    }

    @Test
    public void findDependantsSimple() throws InvalidExpressionException {
        FunctionContext ctx = new FunctionContext("all");

        INodeBoolean node = GenericExpressionCompiler.compileExpressionBoolean("true", ctx);

        DependencyVisitorCollector visitor = DependencyVisitorCollector.createFullSearch();
        visitor.dependOn(node);

        Assert.assertTrue(visitor.areAllConstant());
        Assert.assertFalse(visitor.needsUnkown());
        Assert.assertEquals(0, visitor.getMutableNodes().size());
    }

    @Test
    public void findDependantsSimplevariable() throws InvalidExpressionException {
        FunctionContext ctx = new FunctionContext("all");

        NodeVariableBoolean var = ctx.putVariableBoolean("some_variable");

        INodeBoolean node = GenericExpressionCompiler.compileExpressionBoolean("some_variable", ctx);

        DependencyVisitorCollector visitor = DependencyVisitorCollector.createFullSearch();
        visitor.dependOn(node);

        Assert.assertFalse(visitor.areAllConstant());
        Assert.assertFalse(visitor.needsUnkown());
        Set<IExpressionNode> nodes = visitor.getMutableNodes();
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.contains(var));
    }

    @Test
    public void findDependantsBranch() throws InvalidExpressionException {
        FunctionContext ctx = new FunctionContext("all");

        NodeVariableBoolean var = ctx.putVariableBoolean("some_variable");
        NodeVariableBoolean var2 = ctx.putVariableBoolean("other_variable");

        INodeBoolean node =
            GenericExpressionCompiler.compileExpressionBoolean("some_variable ? true : other_variable", ctx);

        DependencyVisitorCollector visitor = DependencyVisitorCollector.createFullSearch();
        visitor.dependOn(node);

        Assert.assertFalse(visitor.areAllConstant());
        Assert.assertFalse(visitor.needsUnkown());
        Set<IExpressionNode> nodes = visitor.getMutableNodes();
        Assert.assertEquals(2, nodes.size());
        Assert.assertTrue(nodes.contains(var));
        Assert.assertTrue(nodes.contains(var2));
    }

    @Test
    public void findDependantsBranchInline() throws InvalidExpressionException {
        FunctionContext ctx = new FunctionContext("all");

        NodeVariableBoolean var = ctx.putVariableBoolean("some_variable");
        NodeVariableBoolean var2 = ctx.putVariableBoolean("other_variable");

        INodeBoolean node =
            GenericExpressionCompiler.compileExpressionBoolean("true ? some_variable : other_variable", ctx);

        DependencyVisitorCollector visitor = DependencyVisitorCollector.createFullSearch();
        visitor.dependOn(node);

        Assert.assertFalse(visitor.areAllConstant());
        Assert.assertFalse(visitor.needsUnkown());
        Set<IExpressionNode> nodes = visitor.getMutableNodes();
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.contains(var));
        Assert.assertFalse(nodes.contains(var2));
    }
}
