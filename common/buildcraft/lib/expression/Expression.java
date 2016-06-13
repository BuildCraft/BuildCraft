package buildcraft.lib.expression;

import buildcraft.lib.expression.ExpressionCompiler.Node;

public class Expression {
    private final Node node;
    private final long[] variables;

    public Expression(Node node, int numVariables) {
        this.node = node;
        this.variables = new long[numVariables];
    }
    
    public int getNumVariables() {
        return variables.length;
    }

    public long evaluate() {
        return node.evaluate(variables);
    }

    public long evaluate(long[] variables) {
        if (variables.length != this.variables.length) {
            throw new IllegalArgumentException("Invalid length!");
        } else return node.evaluate(variables);
    }
}
