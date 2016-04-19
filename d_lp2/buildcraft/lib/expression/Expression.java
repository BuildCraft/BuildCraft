package buildcraft.lib.expression;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.Maps;

import buildcraft.lib.expression.ExpressionCompiler.Node;

public class Expression {
    private final Node node;
    private final long[] variables;
    private final Map<String, Variable> varAccessor;

    public Expression(Node node, Map<String, Integer> vars) {
        this.node = node;
        this.variables = new long[vars.size()];
        this.varAccessor = Maps.newHashMap();
        for (Entry<String, Integer> entry : vars.entrySet()) {
            varAccessor.put(entry.getKey(), new Variable(entry.getValue()));
        }
    }

    public Map<String, Variable> getVariables() {
        return Collections.unmodifiableMap(varAccessor);
    }

    public Variable getVariable(String name) {
        return varAccessor.get(name);
    }

    public long evaluate() {
        return node.evaluate(variables);
    }

    public class Variable {
        private final int index;

        private Variable(int index) {
            this.index = index;
        }

        public long get() {
            return variables[index];
        }

        public void set(long value) {
            variables[index] = value;
        }
    }
}
