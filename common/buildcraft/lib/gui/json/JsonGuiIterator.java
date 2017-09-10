package buildcraft.lib.gui.json;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;

public class JsonGuiIterator {
    public final String name;
    public final String start;
    public final String end;
    public final String step;

    @Nullable
    public final JsonGuiIterator childIterator;

    public JsonGuiIterator(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            name = JsonUtils.getString(obj, "name", "index");
            start = JsonUtils.getString(obj, "start", "0");
            end = JsonUtils.getString(obj, "end");
            step = JsonUtils.getString(obj, "step", "1");
            if (obj.has("iterator")) {
                childIterator = new JsonGuiIterator(obj.get("iterator"));
            } else {
                childIterator = null;
            }
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String iter = element.getAsString();
            name = iter.substring(0, iter.indexOf('=')).trim();
            String bounds = iter.substring(iter.indexOf('=') + 1);
            start = bounds.substring(0, bounds.indexOf(',')).trim().replace(" ", "");
            end = bounds.substring(bounds.indexOf(',') + 1).trim().replace(" ", "");
            try {
                int s = Integer.parseInt(start.substring(1)) + (start.startsWith("(") ? 1 : 0);
                int e = Integer.parseInt(end.substring(0, end.length() - 1)) - (end.endsWith(")") ? 1 : 0);
                if (s < e) {
                    step = "1";
                } else if (s > e) {
                    step = "-1";
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
}
