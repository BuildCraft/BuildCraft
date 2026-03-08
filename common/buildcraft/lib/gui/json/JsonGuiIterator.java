package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;

import javax.annotation.Nullable;
import java.util.Map;

public class JsonGuiIterator {
    public final String name;
    public final String start;
    public final String step;
    public final String shouldContinue;

    @Nullable
    public final JsonGuiIterator childIterator;

    public JsonGuiIterator(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
//            name = JsonUtils.getString(obj, "name", "index");
            name = JSONUtils.getAsString(obj, "name", "index");
//            start = JsonUtils.getString(obj, "start", "0");
            start = JSONUtils.getAsString(obj, "start", "0");
//            step = JsonUtils.getString(obj, "step");
            step = JSONUtils.getAsString(obj, "step");
            if (obj.has("while")) {
//                shouldContinue = JsonUtils.getString(obj, "while");
                shouldContinue = JSONUtils.getAsString(obj, "while");
            } else {
//                String end = JsonUtils.getString(obj, "end");
                String end = JSONUtils.getAsString(obj, "end");
                shouldContinue = "step > 0 ? ($name <= $end) : ($name >= $end)"//
                        .replace("$end", end).replace("$name", name);
            }
            if (obj.has("iterator")) {
                childIterator = new JsonGuiIterator(obj.get("iterator"));
            } else {
                childIterator = null;
            }
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String iter = element.getAsString();
            name = iter.substring(0, iter.indexOf('=')).trim();
            String bounds = iter.substring(iter.indexOf('=') + 1).trim();
            start = bounds.substring(1, bounds.indexOf(',')).replace(" ", "");
            String end = bounds.substring(bounds.indexOf(',') + 1).trim().replace(" ", "");
            try {
                int s = Integer.parseInt(start) + (start.startsWith("(") ? 1 : 0);
                int e = Integer.parseInt(end.substring(0, end.length() - 1)) - (end.endsWith(")") ? 1 : 0);
                if (s < e) {
                    step = "1";
                    shouldContinue = name + " <= " + e;
                } else if (s > e) {
                    step = "-1";
                    shouldContinue = name + " >= " + e;
                } else {
                    throw new JsonSyntaxException("Don't iterate statically from a value to itself!");
                }
            } catch (NumberFormatException nfe) {
                throw new JsonSyntaxException(nfe);
            }
            childIterator = null;
        } else {
            throw new JsonSyntaxException("Expected an object or a string, got " + element);
        }
    }

    public class ResolvedIterator {
        public final long valStart;
        public final INodeLong valStep;
        public final INodeBoolean valShouldContinue;
        public final NodeVariableLong value;
        private int count = 0;

        @Nullable
        public final ResolvedIterator child;

        public ResolvedIterator(FunctionContext ctx) {
            try {
                valStart = GenericExpressionCompiler.compileExpressionLong(start, ctx).evaluate();
                ctx = new FunctionContext(ctx);
                ctx.putConstantLong("start", valStart);
                value = ctx.putVariableLong(name);
                value.value = valStart;
                valStep = GenericExpressionCompiler.compileExpressionLong(step, ctx);
                ctx.putVariable("step", valStep);
                valShouldContinue = GenericExpressionCompiler.compileExpressionBoolean(shouldContinue, ctx);
            } catch (InvalidExpressionException iee) {
                throw new JsonSyntaxException("Invalid iterator!", iee);
            }
            if (childIterator == null) {
                child = null;
            } else {
                child = childIterator.new ResolvedIterator(ctx);
            }
        }

        public boolean start() {
            value.value = valStart;
            boolean canIterate = valShouldContinue.evaluate();
            if (child != null) {
                canIterate &= child.start();
            }
            return canIterate;
        }

        /** @return True if the iteration has finished, false if not.
         * @throws JsonSyntaxException if {@link #count} exceded 1000 */
        public boolean iterate() {
            count++;
            if (count > 1000) {
                throw new JsonSyntaxException("Too many total iterations (max 1000)!");
            }
            ResolvedIterator c = child;
            if (c != null) {
                if (!c.iterate()) {
                    return false;
                }
                c.value.value = c.valStart;
            }
            long stepValue = valStep.evaluate();
            if (stepValue == 0) {
                throw new JsonSyntaxException("Step was 0!");
            }
            value.value += stepValue;
            return !valShouldContinue.evaluate();
        }

        private JsonGuiIterator getJson() {
            return JsonGuiIterator.this;
        }

        public void putProperties(FunctionContext ctx, Map<String, String> properties) {
            ResolvedIterator iter = this;
            while (iter != null) {
                String n = iter.getJson().name;
                ctx.putConstantLong(n, iter.value.value);
                properties.put(n, iter.value.evaluateAsString());
                iter = iter.child;
            }
        }
    }
}
