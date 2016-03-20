package buildcraft.core.lib.config;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.Maps;

import buildcraft.core.lib.config.ExpressionCompiler.Node;

/** Designates an expression. */
public class Expression {
    private final Node node;
    private final double[] variables;
    private final Map<String, Variable> varAccessor;

    public Expression(Node node, Map<String, Integer> vars) {
        this.node = node;
        this.variables = new double[vars.size()];
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

    public double evaluate() {
        return node.evaluate(variables);
    }

    public class Variable {
        private final int index;

        private Variable(int index) {
            this.index = index;
        }

        public double get() {
            return variables[index];
        }

        public void set(double value) {
            variables[index] = value;
        }
    }
}
